package com.movie.domain.PointHistory.dto.response;

import com.movie.domain.PointHistory.domain.PointHistory;
import com.movie.domain.user.domain.User;

import java.time.LocalDate;

public record GetPointHistoryResDto(
        Long id,
        Integer points,
        String description,
        LocalDate createdAt
) {
    public static GetPointHistoryResDto of(PointHistory pointHistory) {
        return new GetPointHistoryResDto(
                pointHistory.getId(),
                pointHistory.getPoints(),
                pointHistory.getDescription(),
                pointHistory.getCreatedAt()
        );
    }
}
