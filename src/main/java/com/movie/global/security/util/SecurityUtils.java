package com.movie.global.security.util;

import com.movie.domain.user.constant.UserExceptionMessage;
import com.movie.domain.user.dao.UserRepository;
import com.movie.domain.user.domain.User;
import com.movie.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SecurityUtils {
    private final UserRepository userRepository;

    public static String getLoginUserEmail() {
        try {
            Authentication authentication = Objects.requireNonNull(SecurityContextHolder
                    .getContext()
                    .getAuthentication());

            if (authentication instanceof AnonymousAuthenticationToken) {
                authentication = null;
            }

            return authentication.getName();
        } catch (NullPointerException e) {
            throw new RuntimeException();
        }
    }

    public User getLoginUser() {
        try {
            Authentication authentication = Objects.requireNonNull(SecurityContextHolder
                    .getContext()
                    .getAuthentication());

            if (authentication instanceof AnonymousAuthenticationToken) {
                authentication = null;
            }

            return userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new UserNotFoundException(UserExceptionMessage.USER_NOT_FOUND.getMessage()));
        } catch (NullPointerException e) {
            throw new RuntimeException();
        }
    }
}
