package com.movie.domain.user.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import javax.persistence.Id;
import java.util.concurrent.TimeUnit;

@Getter
@RedisHash("refreshToekn")
public class RefreshToken {

    @Id
    private String email;

    private String refreshToken;

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private Long ttl;

    @Builder
    public RefreshToken(String email, String refreshToken, Long ttl) {
        this.email = email;
        this.refreshToken = refreshToken;
        this.ttl = ttl;
    }
}
