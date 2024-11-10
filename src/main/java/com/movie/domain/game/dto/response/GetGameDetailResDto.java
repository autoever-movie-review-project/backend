package com.movie.domain.game.dto.response;

import com.movie.domain.game.domain.Game;
import com.movie.domain.game.domain.GameStatus;
import com.movie.domain.player.dto.response.PlayerInfoDto;

import java.util.List;

public record GetGameDetailResDto(
        Long gameId,
        Long hostId,
        String title,
        GameStatus status,
        Long maxPlayer,
        Long playerCount,

        List<PlayerInfoDto> playerInfo
) {
    public static GetGameDetailResDto of(Game game, List<PlayerInfoDto> playerInfo) {
        return new GetGameDetailResDto(
                game.getId(),
                game.getHostId(),
                game.getTitle(),
                game.getStatus(),
                game.getMaxPlayer(),
                game.getPlayerCount(),
                playerInfo
        );
    }
}
