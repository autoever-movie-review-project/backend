package com.movie.domain.user.exception;

import com.movie.global.exception.DuplicatedException;

public class EmailDuplicatedException extends DuplicatedException {

    public EmailDuplicatedException(String message) {
        super(message);
    }
}
