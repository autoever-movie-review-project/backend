package com.movie.domain.likeReview.dto.response;

import com.movie.domain.review.domain.Review;


public record ReviewDto(
        Long reviewId,
        Long userId,
        String nickname,
        String profile,
        String rankName,
        String rankImg,
        Long movieId,
        String content,
        int likesCount, // 리뷰 좋아요 수
        double rating // 평점
) {
    public static ReviewDto of(Review review) {
        return new ReviewDto(
                review.getReviewId(),
                review.getUser().getUserId(),
                review.getUser().getNickname(),
                review.getUser().getProfile(),
                review.getUser().getRank().getRankName(),
                review.getUser().getRank().getRankImg(),
                review.getMovie().getMovieId(),
                review.getContent(),
                review.getLikesCount(),
                review.getRating()
        );
    }
}
