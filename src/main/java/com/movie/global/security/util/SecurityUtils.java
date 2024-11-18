package com.movie.global.security.util;

import com.movie.global.exception.NotFoundException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static com.movie.global.constant.ExceptionMessage.NOT_FOUND_LOGIN_USER;

@Component
public class SecurityUtils {

    public static String getLoginUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new NotFoundException(NOT_FOUND_LOGIN_USER.getMessage());
        }

        return authentication.getName();
    }
}
