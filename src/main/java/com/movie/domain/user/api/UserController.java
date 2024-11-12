package com.movie.domain.user.api;

import com.movie.domain.user.dto.request.*;
import com.movie.domain.user.dto.response.AuthenticatedResDto;
import com.movie.domain.user.dto.response.CheckResDto;
import com.movie.domain.user.dto.response.TokenInfo;
import com.movie.domain.user.dto.response.UserInfoResDto;
import com.movie.domain.user.service.EmailService;
import com.movie.domain.user.service.UserService;
import com.movie.global.jwt.constant.JwtHeaderUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "user", description = "user domain apis")
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @Value("${jwt.cookieName}")
    private String jwtCookieName;

    @Value("${jwt.refresh-expired-in}")
    private long REFRESH_TOKEN_EXPIRED_IN;

    /**
     * 회원가입 처리
     * @param signUpReqDto 회원가입 요청 데이터
     * @return 성공 시 HTTP 201 상태 반환
     */
    @Operation(summary = "회원가입", description = "필요한 정보를 입력하여 회원 가입합니다.")
    @PostMapping("/signup")
    public ResponseEntity<Void> addUser(@RequestBody @Valid SignUpReqDto signUpReqDto) {
        userService.signUp(signUpReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 이메일 중복 검사
     * @param email 중복 여부를 확인할 이메일
     * @return 중복 검사 결과
     */
    @Operation(summary = "이메일 중복 검사", description = "회원 가입 시 해당 이메일로 이미 가입한 회원이 있는지 검사합니다.")
    @GetMapping("/check-login-email")
    public ResponseEntity<CheckResDto> checkEmail(@RequestParam(name = "email") String email) {
        return ResponseEntity.ok().body(emailService.checkEmailDuplicated(email));
    }

    /**
     * 이메일 인증 코드 전송
     */
    @Operation(summary = "이메일 인증 코드 전송", description = "이메일로 인증 코드를 전송합니다.")
    @PostMapping("/send-email-code")
    public ResponseEntity<Void> sendEmailCode(@RequestParam(name = "email") String email) {
        emailService.sendEmailCode(email);
        return ResponseEntity.ok().build();
    }

    /**
     * 이메일 인증 코드 검사
     */
    @Operation(summary = "이메일 인증 코드 검사", description = "입력한 코드가 전송한 인증 코드와 일치하는지 검사합니다.")
    @PostMapping("/check-email-code")
    public ResponseEntity<CheckResDto> checkEmailCode(@RequestBody CheckEmailCodeReqDto checkEmailCodeReqDto) {
        return ResponseEntity.ok().body(emailService.checkEmailCode(checkEmailCodeReqDto));
    }

    /**
     * 회원 로그인
     * Access Token을 헤더에, Refresh Token을 HttpOnly 쿠키에 설정
     *
     * @param loginReqDto 로그인 요청 데이터
     * @return 로그인 결과
     */
    @Operation(summary = "로그인", description = "아이디와 비밀번호를 입력하여 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<UserInfoResDto> login(@RequestBody LoginReqDto loginReqDto) {

        AuthenticatedResDto authenticatedResDto = userService.login(loginReqDto);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authenticatedResDto.getTokenInfo().getAccessToken());

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", authenticatedResDto.getTokenInfo().getRefreshToken())
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

    /**
     * 토큰 재발급
     * 새로운 Access Token을 헤더에, Refresh Token을 HttpOnly 쿠키에 설정
     */
    @Operation(summary = "토큰 재발급", description = "JWT 토큰을 재발급합니다.")
    @PostMapping("/reissue-token")
    public ResponseEntity<Void> reissueToken(@RequestHeader("Authorization") String accessToken,
                                             @CookieValue(name = "refreshToken") String refreshToken) {
        TokenInfo newTokenInfo = userService.reissueToken(JwtHeaderUtil.extractToken(accessToken), refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, JwtHeaderUtil.GRANT_TYPE.getValue() + " " + newTokenInfo.getAccessToken());

        ResponseCookie newRefreshTokenCookie = ResponseCookie.from("refreshToken", newTokenInfo.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(REFRESH_TOKEN_EXPIRED_IN / 1000)
                .sameSite("Strict")
                .build();
        headers.add(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString());

        return ResponseEntity.ok().headers(headers).build();
    }


    /**
     * 로그아웃 처리
     * Access 및 Refresh 토큰 무효화
     */
    @Operation(summary = "로그아웃", description = "로그아웃 합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String accessToken) {
        userService.logout(JwtHeaderUtil.extractToken(accessToken));
        return ResponseEntity.ok().build();
    }

    /**
     * 회원 정보 수정
     */
    @Operation(summary = "회원 정보 수정", description = "필요한 정보를 입력하여 회원 정보를 수정합니다.")
    @PutMapping("/update")
    public ResponseEntity<Void> updateUser(@RequestBody UpdateUserReqDto updateUserReqDto) {
        userService.updateUser(updateUserReqDto);
        return ResponseEntity.ok().build();
    }

    /**
     * 비밀번호 변경
     */
    @Operation(summary = "비밀번호 수정", description = "회원의 비밀번호를 수정합니다.")
    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestBody UpdatePasswordReqDto updatePasswordReqDto) {
        userService.updatePassword(updatePasswordReqDto);
        return ResponseEntity.ok().build();
    }

    /**
     * 회원 탈퇴
     */
    @Operation(summary = "회원 탈퇴", description = "회원(본인) 탈퇴 합니다.")
    @DeleteMapping
    public ResponseEntity<Void> deleteUser() {
        userService.deleteUser();
        return ResponseEntity.ok().build();
    }

    /**
     * 회원 정보 조회
     */
    @Operation(summary = "회원 정보 조회", description = "회원 정보를 조회합니다.")
    @GetMapping
    public ResponseEntity<UserInfoResDto> findUser() {
        return ResponseEntity.ok().body(userService.findUser());
    }
}
