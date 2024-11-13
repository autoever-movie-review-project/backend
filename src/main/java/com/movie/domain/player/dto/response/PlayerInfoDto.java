package com.movie.domain.player.dto.response;

import com.movie.domain.rank.domain.Rank;
import com.movie.domain.rank.dto.response.InGameRankDto;
import com.movie.domain.user.domain.User;

public record PlayerInfoDto(
        InGameRankDto inGameRankDto,
        String nickname,
        String profile
) {
    public static PlayerInfoDto of(User user, InGameRankDto inGameRankDto) {
        return new PlayerInfoDto(
                inGameRankDto,
                user.getNickname(),
                user.getProfile()
        );
    }
}
