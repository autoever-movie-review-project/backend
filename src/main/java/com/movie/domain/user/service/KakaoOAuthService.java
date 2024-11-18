package com.movie.domain.user.service;

import com.movie.domain.user.dto.response.AuthenticatedResDto;

public interface KakaoOAuthService {
    AuthenticatedResDto processKakaoLogin(String code);
}
