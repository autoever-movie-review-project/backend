package com.movie.global.config;

public final class Constants {

    /**
     * POST 요청에서 인증 없이 접근 가능한 URL 패턴 배열.
     */
    public static final String[] PostPermitArray = new String[]{
            "/api/user",
            "/api/user/((?!auth|checkpw).)+" // 인증이나 비밀번호 확인을 제외한 사용자 관련 API
    };

    /**
     * GET 요청에서 인증 없이 접근 가능한 URL 패턴 배열.
     */
    public static final String[] GetPermitArray = new String[]{
    };

    /**
     * POST, PUT, DELETE 요청에서 관리자 권한으로 접근 가능한 URL 패턴 배열.
     */
    public static final String[] AdminPermitArray = new String[]{
    };
}