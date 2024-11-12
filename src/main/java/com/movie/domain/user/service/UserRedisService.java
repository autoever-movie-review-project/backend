package com.movie.domain.user.service;

import com.movie.domain.user.domain.RefreshToken;
import com.movie.domain.user.dto.response.TokenInfo;

public interface UserRedisService {

    TokenInfo reissueToken(String accessToken, String refreshToken);
    void addRefreshToken(String email, String refreshToken);
    RefreshToken findRefreshToken(String email);
    void validateRefreshToken(String refreshToken, String email);
    void deleteRefreshToken(String email);

//    void addUserInfo(User user);
//
//    UserInfoResDto findUserInfo(String email);
//
//    void deleteUserInfo(User user);
}
