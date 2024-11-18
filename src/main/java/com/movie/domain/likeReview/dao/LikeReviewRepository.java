package com.movie.domain.likeReview.dao;
import com.movie.domain.likeReview.domain.LikeReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikeReviewRepository extends JpaRepository<LikeReview, Long> {
    boolean existsByUserUserIdAndReviewReviewId(Long userId, Long reviewId);

    Long countByReviewReviewId(Long reviewId);

    LikeReview findByUserUserIdAndReviewReviewId(Long userId, Long reviewId);


    List<LikeReview> findAllByUserUserId(Long userId);

}
