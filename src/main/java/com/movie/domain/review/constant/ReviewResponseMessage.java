package com.movie.domain.review.constant;

public enum ReviewResponseMessage {
    ADD_REVIEW("리뷰 작성 완료했습니다."),
    UPDATE_REVIEW("리뷰 수정 완료했습니다."),
    DELETE_REVIEW("리뷰 삭제 완료했습니다."),
    FIND_ONE_REVIEW("리뷰 하나 조회 완료했습니다."),
    FIND_ALL_REVIEWS("리뷰 전체 조회 완료했습니다.");

    private final String message;

    ReviewResponseMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
