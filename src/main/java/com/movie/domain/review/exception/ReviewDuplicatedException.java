package com.movie.domain.review.exception;

public class ReviewDuplicatedException extends RuntimeException {
    public ReviewDuplicatedException() {
        super("리뷰 중복");
    }

    public ReviewDuplicatedException(String message) {
        super(message);
    }
}
