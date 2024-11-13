package com.movie.domain.user.service;

import com.movie.domain.user.dto.request.*;
import com.movie.domain.user.dto.response.AuthenticatedResDto;
import com.movie.domain.user.dto.response.TokenInfo;
import com.movie.domain.user.dto.response.UserInfoResDto;

public interface UserService {
    void signUp(SignUpReqDto userInfoReqDto); //회원가입

    AuthenticatedResDto login(LoginReqDto loginReqDto); //로그인

    void logout(String accessToken); //로그아웃

    UserInfoResDto updateUser(UpdateUserReqDto updateUserReqDto); //회원 정보 수정

    void updateProfile(UpdateProfileReqDto updateProfileReqDto);//프로필 사진 변경

    void deleteUser(); //회원 탈퇴

    void updatePassword(UpdatePasswordReqDto updatePasswordReqDto); //비밀번호 변경

    UserInfoResDto findUser(); //정보 조회

    TokenInfo reissueToken(String accessToken, String refreshToken); //토큰 재발급
}
