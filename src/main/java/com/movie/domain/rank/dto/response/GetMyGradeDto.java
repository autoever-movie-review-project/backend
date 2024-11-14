package com.movie.domain.rank.dto.response;

import com.movie.domain.user.domain.User;

public record GetMyGradeDto(
        String rankName,
        String rankImg,
        Integer points
) {
    public static GetMyGradeDto of (User user) {
        return new GetMyGradeDto(
                user.getRank().getRankName(),
                user.getRank().getRankImg(),
                user.getPoints()
        );
    }
}
