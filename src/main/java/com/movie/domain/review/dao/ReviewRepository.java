package com.movie.domain.review.dao;

import com.movie.domain.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findAllByMovie_MovieId(Long movieId);

    List<Review> findAllByUser_UserId(Long userId);

    Optional<Review> findByReviewId(Long reviewId);

    Page<Review> findByReviewIdIn(List<Long> reviewIds, Pageable pageable);
}