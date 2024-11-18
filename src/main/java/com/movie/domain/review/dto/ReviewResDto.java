package com.movie.domain.review.dto;

import com.movie.domain.review.domain.Review;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ReviewResDto {

    private final Long movieId;
    private final Long reviewId;
    private final Long userId;
    private final String writerNickname;
    private final String content;
    private final String profile;
    private final int likesCount;
    private final double rating;
    private final LocalDate createdAt;

    public static ReviewResDto entityToResDto(Review review) {
        return ReviewResDto.builder()
                .movieId(review.getMovie().getMovieId())
                .reviewId(review.getReviewId())
                .userId(review.getUser().getUserId())
                .writerNickname(review.getUser().getNickname())
                .content(review.getContent())
                .profile(review.getUser() != null ? review.getUser().getProfile() : null)
                .likesCount(review.getLikesCount())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
