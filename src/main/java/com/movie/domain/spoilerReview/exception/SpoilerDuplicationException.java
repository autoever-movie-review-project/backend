package com.movie.domain.spoilerReview.exception;

import com.movie.global.exception.DuplicatedException;

public class SpoilerDuplicationException extends DuplicatedException {
    public SpoilerDuplicationException(String message) {
        super(message);
    }
}
