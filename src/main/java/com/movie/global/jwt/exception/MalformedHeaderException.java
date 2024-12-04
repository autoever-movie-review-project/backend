package com.movie.global.jwt.exception;

public class MalformedHeaderException extends RuntimeException {
    public MalformedHeaderException(String message) {
        super(message);
    }
}
