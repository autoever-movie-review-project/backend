package com.movie.domain.player.dto.response;

import com.movie.domain.player.domain.Player;
import com.movie.domain.rank.domain.Rank;
import com.movie.domain.rank.dto.response.InGameRankDto;
import com.movie.domain.user.domain.User;

public record PlayerInfoDto(
        InGameRankDto inGameRankDto,
        String nickname,
        String profile,
        boolean isReady
) {
    public static PlayerInfoDto of(Player player, User user, InGameRankDto inGameRankDto) {
        return new PlayerInfoDto(
                inGameRankDto,
                user.getNickname(),
                user.getProfile(),
                player.isReady()
        );
    }
}
