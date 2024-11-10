package com.movie.domain.player.exception;

import com.movie.global.exception.DuplicatedException;

public class GameAlreadyStartedException extends DuplicatedException {
    public GameAlreadyStartedException(String message) {
        super(message);
    }
}
