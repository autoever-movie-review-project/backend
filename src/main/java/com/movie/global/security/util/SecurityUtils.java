package com.movie.global.security.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

public class SecurityUtils {
    private final UserRepository userRepository;

    public static String getLoginUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("현재 로그인된 사용자가 없습니다.");
        }

        return authentication.getName();
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
