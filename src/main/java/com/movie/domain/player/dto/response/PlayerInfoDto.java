package com.movie.domain.player.dto.response;

import com.movie.domain.rank.domain.Rank;
import com.movie.domain.user.domain.User;

public record PlayerInfoDto(
        Rank rank,
        String nickname,
        String profile
) {
    public static PlayerInfoDto of(User user) {
        return new PlayerInfoDto(
                user.getRank(),
                user.getNickname(),
                user.getProfile()
        );
    }
}
