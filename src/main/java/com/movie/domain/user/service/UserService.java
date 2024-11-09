package com.movie.domain.user.service;

import com.movie.domain.user.domain.User;
import com.movie.domain.user.dto.request.LoginReqDto;
import com.movie.domain.user.dto.request.UpdatePasswordReqDto;
import com.movie.domain.user.dto.request.UserInfoReqDto;
import com.movie.domain.user.dto.response.LoginResDto;
import com.movie.domain.user.dto.response.TokenInfo;
import com.movie.domain.user.dto.response.UserInfoResDto;

public interface UserService {
    void signUp(UserInfoReqDto userInfoReqDto); //회원가입

    LoginResDto login(LoginReqDto loginReqDto); //로그인

    void logout(String accessToken); //로그아웃

    void updateUser(User user); //회원 정보 수정

    void deleteUser(); //회원 탈퇴

    void updatePassword(UpdatePasswordReqDto updatePasswordReqDto); //비밀번호 변경

    UserInfoResDto findUser(); //정보 조회

    TokenInfo reissueToken(String refreshToken); //토큰 재발급
}
