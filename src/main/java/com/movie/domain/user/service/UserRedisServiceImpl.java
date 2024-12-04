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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.movie.domain.user.constant.UserExceptionMessage.*;

@Slf4j
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
        log.info("[Access Token 파싱] Access Token에서 'Bearer' 제거");
        return accessToken.substring(JwtHeaderUtil.GRANT_TYPE.getValue().length());
    }

    /**
     * Access Token 재발급
     */
    @Override
    public TokenInfo reissueToken(String accessToken, String refreshToken) {
        log.info("[토큰 재발급] 토큰 재발급 요청. Access Token: {}, Refresh Token: {}", accessToken, refreshToken);

        String parsedAccessToken = parseAccessToken(accessToken);
        String email = jwtTokenProvider.getUsernameFromExpiredToken(parsedAccessToken);

        validateRefreshToken(refreshToken, email);
        log.info("[토큰 재발급] Refresh Token 유효성 검사 완료.");

        Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, null);
        TokenInfo newTokenInfo = jwtTokenProvider.generateToken(authentication);
        log.info("[토큰 재발급] 새로운 토큰 생성 완료. Access Token: {}, Refresh Token: {}",
                newTokenInfo.getAccessToken(), newTokenInfo.getRefreshToken());

        addRefreshToken(email, newTokenInfo.getRefreshToken());
        log.info("[토큰 재발급] Redis에 새로운 Refresh Token 저장 완료. email: {}", email);

        return newTokenInfo;
    }

    /**
     * Refresh Token 검증
     */
    @Override
    public void validateRefreshToken(String refreshToken, String email) {
        log.info("[Refresh Token 검증] Refresh Token 유효성 검사 시작. email: {}", email);

        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            log.error("[Refresh Token 검증] Refresh Token 만료됨.");
            throw new ExpiredTokenException(REFRESH_TOKEN_EXPIRED.getMessage());
        }

        String storedRefreshToken = findRefreshToken(email).getRefreshToken();
        log.info("[Refresh Token 검증] Redis에 저장된 Refresh Token과 비교.");

        if (!storedRefreshToken.equals(refreshToken)) {
            log.error("[Refresh Token 검증] 토큰 불일치. 저장된 토큰과 일치하지 않음.");
            throw new TokenException(TOKEN_MISMATCH.getMessage());
        }

        log.info("[Refresh Token 검증] Refresh Token 검증 성공.");
    }

    /**
     * Redis에서 Refresh Token 찾기
     */
    @Override
    public RefreshToken findRefreshToken(String email) {
        log.info("[Redis Refresh Token 검색] 이메일에 대한 Refresh Token 검색. email: {}", email);
        return refreshTokenRedisRepository.findById(email)
                .orElseThrow(() -> {
                    log.error("[Redis Refresh Token 검색] 해당 이메일에 대한 Refresh Token 없음.");
                    return new NotFoundException(REFRESH_TOKEN_NOT_FOUND.getMessage());
                });
    }

    /**
     * Redis에 Refresh Token 저장 또는 업데이트
     */
    @Transactional
    @Override
    public void addRefreshToken(String email, String refreshToken) {
        log.info("[Refresh Token 저장] Redis에 Refresh Token 저장 또는 업데이트. email: {}", email);
        refreshTokenRedisRepository.save(
                RefreshToken.builder()
                        .email(email)
                        .refreshToken(refreshToken)
                        .expiration(REFRESH_TOKEN_EXPIRED_IN / 1000)
                        .build()
        );
        log.info("[Refresh Token 저장] Refresh Token 저장 완료.");
    }

    /**
     * Redis에서 Refresh Token 삭제
     */
    @Transactional
    @Override
    public void deleteRefreshToken(String email) {
        log.info("[Refresh Token 삭제] Redis에서 Refresh Token 삭제 요청. email: {}", email);
        refreshTokenRedisRepository.deleteById(email);
        log.info("[Refresh Token 삭제] Refresh Token 삭제 완료.");
    }
}
