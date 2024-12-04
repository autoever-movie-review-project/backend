package com.movie.domain.likeReview.exception;

import com.movie.global.exception.DuplicatedException;

public class LikeReviewDuplicateException extends DuplicatedException {
    public LikeReviewDuplicateException(String message) {
        super(message);
    }
}
