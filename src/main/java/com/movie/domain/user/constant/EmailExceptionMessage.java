package com.movie.domain.user.constant;

public enum EmailExceptionMessage {
    EMAIL_DUPLICATED("이미 이메일이 존재합니다."),
    EMAIL_NOT_SENT("이메일 발송을 실패하였습니다."),
    EMAIL_CODE_NOT_FOUND("해당 이메일로 유효한 인증 코드가 존재하지 않습니다."),
    EMAIL_CODE_NOT_VALID("이메일 인증 코드가 일치하지 않습니다."),
    EMAIL_CHECK_FAILED("이메일 인증을 해야 회원가입을 할 수 있습니다.");
    private final String message;
    EmailExceptionMessage(String message) {this.message = message;}
    public String getMessage() {return message;}
}
