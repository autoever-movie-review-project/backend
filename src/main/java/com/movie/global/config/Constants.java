package com.movie.global.config;

public final class Constants {

    /**
     * POST 요청에서 인증 없이 접근 가능한 URL 패턴 배열.
     */
    public static final String[] PostPermitArray = new String[]{
            "/user/signup",
            "/user/login",
            "/user/send-email-code.*",
            "/user/check-email-code",
            "/user/reissue-token",
            "/movie/initialize"
    };

    /**
     * GET 요청에서 인증 없이 접근 가능한 URL 패턴 배열.
     */
    public static final String[] GetPermitArray = new String[]{
            "/user/check-login-email",
            "/user/email"
    };

    /**
     * POST, PUT, DELETE 요청에서 관리자 권한으로 접근 가능한 URL 패턴 배열.
     */
    public static final String[] AdminPermitArray = new String[]{
            // 관리자 권한이 필요한 엔드포인트가 있을 경우 추가
    };
}
