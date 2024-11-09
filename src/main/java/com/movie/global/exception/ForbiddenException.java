package com.movie.global.exception;

public class ForbiddenException extends SecurityException {
    public ForbiddenException(String message) {
        super(message);
    }
}
