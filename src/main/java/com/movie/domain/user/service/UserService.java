package com.movie.domain.user.service;

import com.movie.domain.user.dto.request.LoginReqDto;
import com.movie.domain.user.dto.request.SignUpReqDto;
import com.movie.domain.user.dto.request.UpdatePasswordReqDto;
import com.movie.domain.user.dto.request.UpdateUserReqDto;
import com.movie.domain.user.dto.response.AuthenticatedResDto;
import com.movie.domain.user.dto.response.LoginResDto;
import com.movie.domain.user.dto.response.TokenInfo;
import com.movie.domain.user.dto.response.UserInfoResDto;

public interface UserService {
    void signUp(SignUpReqDto userInfoReqDto); //회원가입

    AuthenticatedResDto login(LoginReqDto loginReqDto); //로그인

    void logout(String accessToken); //로그아웃

    UserInfoResDto updateUser(UpdateUserReqDto updateUserReqDto); //회원 정보 수정

    void deleteUser(); //회원 탈퇴

    void updatePassword(UpdatePasswordReqDto updatePasswordReqDto); //비밀번호 변경

    UserInfoResDto findUser(); //정보 조회

    TokenInfo reissueToken(String accessToken, String refreshToken); //토큰 재발급
}
