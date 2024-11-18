package com.movie.domain.user.api;

import com.movie.domain.user.dto.response.AuthenticatedResDto;
import com.movie.domain.user.dto.response.TokenInfo;
import com.movie.domain.user.dto.response.UserInfoResDto;
import com.movie.domain.user.service.KakaoOAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kakao")
@RequiredArgsConstructor
public class KakaoOAuthController {

    private final KakaoOAuthService kakaoOAuthService;

    @Value("${jwt.cookieName}")
    private String COOKIE_NAME;

    @Value("${jwt.refresh-expired-in}")
    private long REFRESH_TOKEN_EXPIRED_IN;

    @GetMapping
    public ResponseEntity<UserInfoResDto> kakaoCallback(@RequestParam String code) {
        // 카카오 로그인 처리
        AuthenticatedResDto authenticatedResDto = kakaoOAuthService.processKakaoLogin(code);

        // Access Token을 헤더에 추가
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authenticatedResDto.getTokenInfo().getAccessToken());

        // Refresh Token을 HttpOnly 쿠키로 담음
        ResponseCookie refreshTokenCookie = ResponseCookie.from(COOKIE_NAME, authenticatedResDto.getTokenInfo().getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(REFRESH_TOKEN_EXPIRED_IN / 1000)
                .sameSite("Strict")
                .build();
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(authenticatedResDto.getUserInfo());
    }
}
