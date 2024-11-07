package com.movie.domain.user.dto.response;

import com.movie.domain.user.domain.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticatedResDto {
    private final TokenInfo tokens;
    private final UserInfoResDto userInfo;

    public static AuthenticatedResDto entityToResDto(TokenInfo tokens, User user) {
        UserInfoResDto userInfo = UserInfoResDto.entityToResDto(user);
        return AuthenticatedResDto.builder()
                .tokens(tokens)
                .userInfo(userInfo)
                .build();
    }
}