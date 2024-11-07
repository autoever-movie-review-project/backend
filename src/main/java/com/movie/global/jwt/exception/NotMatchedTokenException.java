package com.movie.global.jwt.exception;

public class NotMatchedTokenException extends RuntimeException {
    public NotMatchedTokenException(String message) {
        super(message);
    }
}
