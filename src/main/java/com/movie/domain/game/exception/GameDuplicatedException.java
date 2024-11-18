package com.movie.domain.game.exception;

import com.movie.global.exception.DuplicatedException;

public class GameDuplicatedException extends DuplicatedException {
    public GameDuplicatedException(String message) {
        super(message);
    }
}
