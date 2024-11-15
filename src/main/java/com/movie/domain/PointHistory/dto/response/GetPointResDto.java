package com.movie.domain.PointHistory.dto.response;

import com.movie.domain.PointHistory.domain.PointHistory;
import com.movie.domain.user.domain.User;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

public record GetPointResDto(
        Long id,
        Integer points,
        String description,
        LocalDate createdAt
) {
    public static GetPointResDto of(PointHistory pointHistory) {
        return new GetPointResDto(
                pointHistory.getId(),
                pointHistory.getPoints(),
                pointHistory.getDescription(),
                pointHistory.getCreatedAt()
        );
    }
}
