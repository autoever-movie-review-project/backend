package com.movie.domain.like.exception;

import com.movie.global.exception.DuplicatedException;

public class LikeMovieDuplicateException extends DuplicatedException {
    public LikeMovieDuplicateException(String message) {
        super(message);
    }
}
