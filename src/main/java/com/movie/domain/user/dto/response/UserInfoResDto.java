package com.movie.domain.user.dto.response;

import com.movie.domain.user.domain.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoResDto {
    private String email;
    private String nickname;
    private String profile;
    private int points;
    private String rankName;
    private String rankImg;

    public static UserInfoResDto entityToResDto(User user) {
        return UserInfoResDto.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profile(user.getProfile())
                .points(user.getPoints())
                .rankName(user.getRank().getRankName())
                .rankImg(user.getRank().getRankImg())
                .build();
    }
}
