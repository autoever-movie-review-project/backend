package com.movie.domain.pointHistory.dto.response;

import com.movie.domain.pointHistory.domain.PointHistory;
import com.movie.domain.user.domain.User;

import java.time.LocalDate;

public record GetPointResDto(
        Long id,
        Integer points,
        Integer totalPoints,
        String description,
        LocalDate createdAt
) {
    public static GetPointResDto of(User user, PointHistory pointHistory) {
        return new GetPointResDto(
                pointHistory.getId(),
                pointHistory.getPoints(),
                user.getPoints(),
                pointHistory.getDescription(),
                pointHistory.getCreatedAt()
        );
    }
}
