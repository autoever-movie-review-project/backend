package com.movie.domain.player.dto.response;

import com.movie.domain.user.domain.User;

public record IsReadyPlayerResDto(
        Long userId,
        boolean isReady
) {
    public static IsReadyPlayerResDto of(User user, boolean isReady) {
        return new IsReadyPlayerResDto(
                user.getUserId(),
                isReady
        );
    }
}
