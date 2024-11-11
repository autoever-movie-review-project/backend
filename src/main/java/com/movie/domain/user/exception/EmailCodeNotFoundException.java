package com.movie.domain.user.exception;

import com.movie.global.exception.NotFoundException;

public class EmailCodeNotFoundException extends NotFoundException {
    public EmailCodeNotFoundException(String message) {
        super(message);
    }
}