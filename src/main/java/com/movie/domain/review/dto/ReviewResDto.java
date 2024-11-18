package com.movie.domain.review.dto;

import com.movie.domain.likeReview.domain.LikeReview;
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
    private final String rankName;
    private final String title;
    private final String mainImg;
    private final int likesCount;
    private final double rating;
    private final boolean liked;
    private final LocalDate createdAt;

    public static ReviewResDto entityToResDto(Review review, boolean liked) {
        return ReviewResDto.builder()
                .movieId(review.getMovie().getMovieId())
                .reviewId(review.getReviewId())
                .userId(review.getUser().getUserId())
                .rankName(review.getUser().getRank().getRankName())
                .writerNickname(review.getUser().getNickname())
                .content(review.getContent())
                .profile(review.getUser() != null ? review.getUser().getProfile() : null)
                .title(review.getMovie().getTitle())
                .mainImg(review.getMovie().getMainImg())
                .likesCount(review.getLikesCount())
                .rating(review.getRating())
                .liked(liked)
                .createdAt(review.getCreatedAt())
                .build();
    }
}
