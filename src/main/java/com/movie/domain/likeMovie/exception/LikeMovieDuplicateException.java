package com.movie.domain.likeMovie.exception;

import com.movie.global.exception.DuplicatedException;

public class LikeMovieDuplicateException extends DuplicatedException {
    public LikeMovieDuplicateException(String message) {
        super(message);
    }
}
