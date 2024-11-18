package com.movie.domain.review.exception;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException() {
        super("리뷰 조회 불가");
    }

    public ReviewNotFoundException(String message) {
        super(message);
    }
}
