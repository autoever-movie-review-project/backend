package com.movie.domain.user.service;

import com.movie.domain.user.domain.RefreshToken;

public interface UserRedisService {
    void addRefreshToken(String email, String refreshToken);

    RefreshToken findRefreshToken(String email);

    void deleteRefreshToken(String email);

//    void addUserInfo(User user);
//
//    UserInfoResDto findUserInfo(String email);
//
//    void deleteUserInfo(User user);
}
