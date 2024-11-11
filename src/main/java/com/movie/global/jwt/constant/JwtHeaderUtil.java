package com.movie.global.jwt.constant;

import lombok.Getter;

@Getter
public enum JwtHeaderUtil {
    AUTHORIZATION("Authorization 헤더", "Authorization"),
    GRANT_TYPE("JWT 타입 / Bearer ", "Bearer ");

    private final String description;
    private final String value;

    JwtHeaderUtil(String description, String value) {
        this.description = description;
        this.value = value;
    }

    /**
     * Authorization 헤더에서 "Bearer " 부분을 제거하고 순수 토큰만 반환합니다.
     *
     * @param token Authorization 헤더의 값
     * @return Bearer를 제거한 순수 토큰 값
     */
    public static String extractToken(String token) {
        if (token != null && token.startsWith(GRANT_TYPE.getValue())) {
            return token.substring(GRANT_TYPE.getValue().length()).trim();
        }
        return token;
    }
}
