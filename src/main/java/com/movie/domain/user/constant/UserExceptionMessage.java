package com.movie.domain.user.constant;

public enum UserExceptionMessage {

    SIGN_UP_NOT_VALID("회원가입에 필요한 정보가 입력되지 않았습니다."),
    EMAIL_DUPLICATED("이미 이메일이 존재합니다."),
    EMAIL_CHECK_FAILED("이메일 인증을 해야 회원가입을 할 수 있습니다."),
    USER_NOT_FOUND("존재하지 않는 회원입니다."),
    LOGIN_PASSWORD_ERROR("비밀번호가 일치하지 않습니다.");

    private final String message;
    UserExceptionMessage(String message) {this.message = message;}
    public String getMessage() {return message;}
}
