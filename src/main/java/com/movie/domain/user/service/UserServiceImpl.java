package com.movie.domain.user.service;

import com.movie.domain.user.dao.UserRepository;
import com.movie.domain.user.domain.User;
import com.movie.domain.user.dto.request.LoginReqDto;
import com.movie.domain.user.dto.request.UpdatePasswordReqDto;
import com.movie.domain.user.dto.response.LoginResDto;
import com.movie.domain.user.dto.response.TokenInfo;
import com.movie.domain.user.dto.response.UserInfoResDto;
import com.movie.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRedisService userRedisService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public void signUp(User user) {

    }

    @Override
    public LoginResDto login(LoginReqDto loginReqDto) {
        return null;
    }

    @Override
    public void logout(String accessToken) {

    }

    @Override
    public void updateUser(User user) {

    }

    @Override
    public void deleteUser() {

    }

    @Override
    public void updatePassword(UpdatePasswordReqDto updatePasswordReqDto) {

    }

    @Override
    public UserInfoResDto findUser() {
        return null;
    }

    @Override
    public TokenInfo reissueToken(String refreshToken) {
        return null;
    }
}
