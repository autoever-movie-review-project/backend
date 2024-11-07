package com.movie.domain.user.service;

import com.movie.domain.user.dao.LogoutAccessTokenRedisRepository;
import com.movie.domain.user.dao.RefreshTokenRedisRepository;
import com.movie.domain.user.dto.request.CheckEmailCodeReqDto;
import com.movie.domain.user.dto.response.CheckResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final RefreshTokenRedisRepository refreshTokenRepository;
    private final LogoutAccessTokenRedisRepository logoutAccessTokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JavaMailSender javaMailSender;

    @Override
    public void sendEmailCode(String email) {

    }

    @Override
    public CheckResDto checkEmailDuplicated(String email) {
        return null;
    }

    @Override
    public CheckResDto checkEmailCode(CheckEmailCodeReqDto checkEmailCodeReqDto) {
        return null;
    }
}
