package com.movie.domain.likeReview.dao;

import com.movie.domain.likeReview.domain.LikeReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeReviewRepository extends JpaRepository<LikeReview, Long> {
}
