package com.movie.domain.user.service;

import com.movie.domain.user.dao.RefreshTokenRedisRepository;
import com.movie.domain.user.dao.UserInfoRedisRepository;
import com.movie.domain.user.domain.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRedisServiceImpl implements UserRedisService {

    private final UserInfoRedisRepository userInfoRedisRepository;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    @Value("${jwt.refresh-expired-in}")
    private long REFRESH_TOKEN_EXPIRED_IN;
    @Value("${spring.redis.ttls.user-info}")
    private long USER_INFO_EXPIRED_IN;

    @Override
    @Transactional
    public void addRefreshToken(String email, String refreshToken) {
        refreshTokenRedisRepository.save(RefreshToken.builder()
                .email(email)
                .refreshToken(refreshToken)
                .ttl(REFRESH_TOKEN_EXPIRED_IN)
                .build());
    }


    @Override
    public RefreshToken findRefreshToken(String email) {
        return refreshTokenRedisRepository.findByEmail(email)
                .orElse(null);
    }

    @Override
    public void deleteRefreshToken(String email) {
        refreshTokenRedisRepository.delete(
                RefreshToken.builder().email(email).build()
        );
    }
}
