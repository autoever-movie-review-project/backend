package com.movie.domain.user.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import javax.persistence.Id;

@Getter
@RedisHash("logoutAccessToken")
public class LogoutAccessToken {

    @Id
    private String email;

    private String accessToken;

    @TimeToLive
    private Long ttl;

    @Builder
    public LogoutAccessToken(String email, String accessToken, Long ttl) {
        this.email = email;
        this.accessToken = accessToken;
        this.ttl = ttl;
    }
}
