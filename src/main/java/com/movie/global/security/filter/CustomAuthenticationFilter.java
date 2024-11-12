package com.movie.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.domain.user.domain.RefreshToken;
import com.movie.domain.user.dto.response.TokenInfo;
import com.movie.domain.user.service.UserRedisService;
import com.movie.global.dto.ResponseDto;
import com.movie.global.exception.TokenException;
import com.movie.global.jwt.JwtTokenProvider;
import com.movie.global.jwt.constant.JwtHeaderUtil;
import com.movie.global.jwt.constant.JwtResponseMessage;
import com.movie.global.jwt.dto.Token;
import com.movie.global.jwt.exception.MalformedHeaderException;
import com.movie.global.jwt.exception.TokenNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.movie.global.jwt.constant.JwtExceptionMessage.MALFORMED_HEADER;
import static com.movie.global.jwt.constant.JwtExceptionMessage.TOKEN_NOTFOUND;
import static com.movie.global.jwt.constant.TokenType.ACCESS;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final UserRedisService userRedisService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private static final String UTF_8 = "utf-8";


    /**
     * doFilterInternal 메서드는 HTTP 요청에 포함된 Access Token을 검증하고 인증을 설정합니다.
     * 만료된 경우 Redis에 저장된 Refresh Token을 통해 새로운 Access Token을 재발급합니다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        log.debug("Request URI: {}", requestURI);

        // 허용할 URL 경로 배열
        String[] permitUrls = {
                "/api/user/signup",
                "/api/user/login",
                "/api/user/logout",
                "/api/user/update",
                "/api/user/send-email-code",
                "/api/user/check-email-code",
                "/api/user/check-login-email",
                "/api/user/reissue-token",
        };

        // 현재 요청 URI가 허용할 URL 경로 중 하나인지 확인
        for (String url : permitUrls) {
            if (requestURI.startsWith(url)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 허용된 URL이 아닌 경우 토큰 처리
        try {
            Token token = resolveAccessToken(request);

            // Access Token이 유효한 경우 SecurityContext에 인증 정보를 설정합니다.
            if (token != null && jwtTokenProvider.validateToken(token.getToken())) {
                Authentication authentication = jwtTokenProvider.getAuthentication(token.getToken());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Access Token이 만료된 경우 Refresh Token을 사용해 Access Token을 재발급합니다.
            } else if (token != null && !jwtTokenProvider.validateToken(token.getToken())) {
                handleExpiredAccessToken(request, response);
                return;
            }

            filterChain.doFilter(request, response);
        } catch (TokenException e) {
            makeTokenExceptionResponse(response, e);
        }
    }


    /**
     * Access Token이 만료된 경우 Redis에 저장된 Refresh Token을 통해 새로운 Access Token을 발급합니다.
     */
    private void handleExpiredAccessToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String refreshToken = getRefreshTokenFromRedis(request);

        if (refreshToken != null && jwtTokenProvider.validateRefreshToken(refreshToken)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
            TokenInfo tokenInfo = reissueTokensAndSaveOnRedis(authentication);

            makeTokenInfoResponse(response, tokenInfo);
        } else {
            throw new TokenNotFoundException(TOKEN_NOTFOUND.getMessage());
        }
    }

    /**
     * Authorization 헤더에서 Access Token을 추출하여 반환합니다.
     * 헤더가 유효하지 않은 형식일 경우 예외를 발생시킵니다.
     */
    private Token resolveAccessToken(HttpServletRequest request) {
        String token = request.getHeader(JwtHeaderUtil.AUTHORIZATION.getValue());
        if (StringUtils.hasText(token) && token.startsWith(JwtHeaderUtil.GRANT_TYPE.getValue())) {
            return Token.builder()
                    .tokenType(ACCESS)
                    .token(token.substring(JwtHeaderUtil.GRANT_TYPE.getValue().length()))
                    .build();
        }
        throw new MalformedHeaderException(MALFORMED_HEADER.getMessage());
    }

    /**
     * Redis에서 저장된 Refresh Token을 가져옵니다.
     * Access Token이 만료된 경우에만 사용됩니다.
     */
    private String getRefreshTokenFromRedis(HttpServletRequest request) {
        // 만료된 Access Token을 통해 사용자 이름을 추출하고, 해당 이름을 사용해 Redis에서 Refresh Token을 가져옵니다.
        String username = jwtTokenProvider.getUsernameFromExpiredToken(request.getHeader(JwtHeaderUtil.AUTHORIZATION.getValue()));
        RefreshToken storedRefreshToken = userRedisService.findRefreshToken(username);

        if (storedRefreshToken == null || !StringUtils.hasText(storedRefreshToken.getRefreshToken())) {
            throw new TokenNotFoundException(TOKEN_NOTFOUND.getMessage());
        }
        return storedRefreshToken.getRefreshToken();
    }

    /**
     * TokenException 발생 시 예외 메시지를 담아 클라이언트에 응답을 반환합니다.
     */
    private void makeTokenExceptionResponse(HttpServletResponse response, TokenException e)
            throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(UTF_8);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.getWriter().write(
                objectMapper.writeValueAsString(
                        ResponseDto.create(e.getMessage())
                )
        );
    }

    /**
     * 새로운 Access Token을 발급하고 Redis에 Refresh Token을 갱신하여 저장합니다.
     */
    private TokenInfo reissueTokensAndSaveOnRedis(Authentication authentication) {
        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);
        userRedisService.addRefreshToken(authentication.getName(), tokenInfo.getRefreshToken());
        return tokenInfo;
    }

    /**
     * 새로 발급된 Access Token을 클라이언트에게 응답합니다.
     * 메시지는 JwtResponseMessage.TOKEN_REISSUED에 정의된 메시지를 사용합니다.
     */
    private void makeTokenInfoResponse(HttpServletResponse response, TokenInfo tokenInfo)
            throws IOException {
        response.setStatus(HttpStatus.CREATED.value());
        response.setCharacterEncoding(UTF_8);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.getWriter().write(
                objectMapper.writeValueAsString(
                        ResponseDto.create(JwtResponseMessage.TOKEN_REISSUED.getMessage(), tokenInfo)
                )
        );
    }
}