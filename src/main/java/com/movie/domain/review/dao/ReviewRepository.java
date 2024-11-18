package com.movie.domain.review.dao;

import com.movie.domain.likeReview.dto.response.ReviewDto;
import com.movie.domain.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.movie.movieId = :movieId")
    List<Review> findAllByMovieId(@Param("movieId") Long movieId);

    @Query("SELECT r FROM Review r JOIN FETCH r.movie WHERE r.user.userId = :userId")
    List<Review> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Review r JOIN FETCH r.user JOIN FETCH r.movie WHERE r.reviewId = :reviewId")
    Optional<Review> findByIdWithDetails(@Param("reviewId") Long reviewId);

    @Query("SELECT r FROM Review r WHERE r.reviewId IN :reviewIds")
    Page<Review> findByReviewIds(@Param("reviewIds") List<Long> reviewIds, Pageable pageable);

    Optional<Review> findByReviewId(Long reviewId);

    Page<Review> findByReviewIdIn(List<Long> reviewIds, Pageable pageable);
}

