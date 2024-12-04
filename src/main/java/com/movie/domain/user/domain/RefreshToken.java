package com.movie.domain.user.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

@Getter
@RedisHash("refreshToken")
public class RefreshToken {

    @Id
    private String email;

    private String refreshToken;

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private long expiration;

    @Builder
    public RefreshToken(String email, String refreshToken, long expiration) {
        this.email = email;
        this.refreshToken = refreshToken;
        this.expiration = expiration;
    }
}
