package com.movie.domain.user.domain;

import com.movie.domain.user.constant.UserType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@RedisHash(value = "user_info")
public class UserInfo {

    @Id
    private String userId;
    private String email;
    private Integer emailAlert;
    private String nickname;
    private String profileImg;
    private Integer point;
    private UserType userType;

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private Long ttl;

}
