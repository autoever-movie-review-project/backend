package com.movie.domain.user.service;

import com.movie.domain.user.dao.RefreshTokenRedisRepository;
import com.movie.domain.user.domain.RefreshToken;
import com.movie.domain.user.dto.response.TokenInfo;
import com.movie.global.exception.NotFoundException;
import com.movie.global.exception.TokenException;
import com.movie.global.jwt.JwtTokenProvider;
import com.movie.global.jwt.constant.JwtHeaderUtil;
import com.movie.global.jwt.exception.ExpiredTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRedisServiceImpl implements UserRedisService {

    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.refresh-expired-in}")
    private long REFRESH_TOKEN_EXPIRED_IN;

    /**
     * Bearer 떼고 Access Token 가져옴
     */
    private String parseAccessToken(String accessToken) {
        return accessToken.substring(JwtHeaderUtil.GRANT_TYPE.getValue().length());
    }

    /**
     * Access Token 재발급
     */
    @Override
    public TokenInfo reissueToken(String accessToken, String refreshToken) {
        // Access Token에서 이메일 추출
        String parsedAccessToken = parseAccessToken(accessToken);
        String email = jwtTokenProvider.getUsernameFromExpiredToken(parsedAccessToken);

        // Refresh Token 검증
        validateRefreshToken(refreshToken, email);

        // 새로운 토큰 생성 및 저장
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, null);
        TokenInfo newTokenInfo = jwtTokenProvider.generateToken(authentication);

        // Redis에 새로운 Refresh Token 저장
        addRefreshToken(email, newTokenInfo.getRefreshToken());

        return newTokenInfo;
    }

    /**
     * Refresh Token 검증
     */
    @Override
    public void validateRefreshToken(String refreshToken, String email) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new ExpiredTokenException("리프레시 토큰이 만료되었습니다. 로그인을 다시 해주세요.");
        }

        String storedRefreshToken = findRefreshToken(email).getRefreshToken();
        if (!storedRefreshToken.equals(refreshToken)) {
            throw new TokenException("토큰이 일치하지 않습니다.");
        }
    }

    /**
     * Redis에서 Refresh Token 찾기
     */
    @Override
    public RefreshToken findRefreshToken(String email) {
        return refreshTokenRedisRepository.findById(email)
                .orElseThrow(() -> new NotFoundException("해당 이메일에 대한 토큰이 존재하지 않습니다."));
    }

    /**
     * Redis에 Refresh Token 저장 또는 업데이트
     */
    @Transactional
    @Override
    public void addRefreshToken(String email, String refreshToken) {
        refreshTokenRedisRepository.save(
                RefreshToken.builder()
                        .email(email)
                        .refreshToken(refreshToken)
                        .expiration(REFRESH_TOKEN_EXPIRED_IN / 1000)
                        .build()
        );
    }
}
