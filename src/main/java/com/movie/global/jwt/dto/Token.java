package com.movie.global.jwt.dto;

import com.movie.global.jwt.constant.TokenType;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class Token {
    private final TokenType tokenType;
    private final String token;
}
