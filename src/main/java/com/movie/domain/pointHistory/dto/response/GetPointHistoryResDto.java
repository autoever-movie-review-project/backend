package com.movie.domain.pointHistory.dto.response;

import com.movie.domain.pointHistory.domain.PointHistory;

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
