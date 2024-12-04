package com.movie.domain.player.exception;

import com.movie.global.exception.DuplicatedException;

public class GameRoomFullException extends DuplicatedException {
    public GameRoomFullException(String message) {
        super(message);
    }
}
