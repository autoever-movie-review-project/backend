package com.movie.domain.likeReview.dao;
import com.movie.domain.likeReview.domain.LikeReview;
import com.movie.domain.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikeReviewRepository extends JpaRepository<LikeReview, Long> {
    boolean existsByUserUserIdAndReviewId(Long userId, Long reviewId);

    Long countByReviewId(Long reviewId);

    LikeReview findByUserUserIdAndReviewId(Long userId, Long reviewId);


    List<LikeReview> findAllByUserUserId(Long userId);

}
