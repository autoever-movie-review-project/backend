package com.movie.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResDto {
    private String accessToken;
    private String refreshToken;
    private String nickname;
    private String profile;
}
