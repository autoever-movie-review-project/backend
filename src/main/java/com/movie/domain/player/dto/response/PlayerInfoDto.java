package com.movie.domain.player.dto.response;

import com.movie.domain.user.domain.User;

public record PlayerInfoDto(
        Long rankId,
        String nickname,
        String profile
) {
    public static PlayerInfoDto of(User user) {
        return new PlayerInfoDto(
                user.getRankId(),
                user.getNickname(),
                user.getProfile()
        );
    }
}
