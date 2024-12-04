package com.movie.domain.spoilerReview.dao;

import com.movie.domain.spoilerReview.domain.SpoilerReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpoilerReviewRepository extends JpaRepository<SpoilerReview, Long> {
    Long countByReviewReviewId(Long reviewId);

    boolean existsByUserUserIdAndReviewReviewId(Long userId, Long reviewId);

}
