package com.movie.domain.user.dto.response;

import com.movie.domain.user.constant.UserType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResDto {
    private String userType;
    private Long userId;
    private String nickname;
    private String profile;
    private int points;
    private String rankName;
    private String rankImg;
}
