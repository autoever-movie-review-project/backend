package com.movie.domain.review.constant;

public enum ReviewExceptionMessage {
    REVIEW_NOT_FOUND("해당 ID의 리뷰를 찾을 수 없습니다."),
    REVIEW_USER_NOT_FOUND("해당 리뷰의 작성자를 찾을 수 없습니다."),
    REVIEW_MOVIE_NOT_FOUND("해당 리뷰의 영화를 찾을 수 없습니다."),
    RANK_NOT_FOUND_FOR_POINTS("해당 포인트 구간의 랭크를 찾을 수 없습니다."),
    NO_PERMISSION("이 작업을 수행할 권한이 없습니다.");

    private final String message;

    ReviewExceptionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
