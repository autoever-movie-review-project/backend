package com.movie.domain.movie.exception;

import com.movie.global.exception.NotFoundException;

public class MovieNotFoundException extends NotFoundException {
    public MovieNotFoundException(String message) {
        super(message);
    }
}