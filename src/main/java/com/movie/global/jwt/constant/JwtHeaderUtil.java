package com.movie.global.jwt.constant;

import lombok.Getter;

@Getter
public enum JwtHeaderUtil {
    AUTHORIZATION("Authorization 헤더 ", "Authorization"),
    GRANT_TYPE("JWT 타입 / Bearer ", "Bearer ");

    private final String description;
    private final String value;

    JwtHeaderUtil(String description, String value) {
        this.description = description;
        this.value = value;
    }
}
