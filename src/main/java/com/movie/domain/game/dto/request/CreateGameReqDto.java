package com.movie.domain.game.dto.request;

public record CreateGameReqDto(
        String title,
        Integer maxPlayer
) {

}
