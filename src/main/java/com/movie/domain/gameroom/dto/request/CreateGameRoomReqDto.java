package com.movie.domain.gameroom.dto.request;

public record CreateGameRoomReqDto(
        String title,
        Integer maxPlayer
) {

}
