package com.movie.global.constant;

public enum ExceptionMessage {
    AUTHENTICATION_FAILED("인증 실패했습니다."),
    AUTHORIZATION_FAILED("접근 권한이 없습니다."),
    NOT_FOUND_LOGIN_USER("현재 로그인된 사용자가 없습니다.");
    ;

    private final String message;

    ExceptionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
