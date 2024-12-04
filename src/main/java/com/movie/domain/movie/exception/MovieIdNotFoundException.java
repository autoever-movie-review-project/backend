package com.movie.domain.movie.exception;

import com.movie.global.exception.NotFoundException;

public class MovieIdNotFoundException extends NotFoundException {
    public MovieIdNotFoundException(String message) {
        super(message);
    }
}
