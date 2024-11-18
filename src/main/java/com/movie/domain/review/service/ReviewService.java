package com.movie.domain.review.service;

import com.movie.domain.review.dto.ReviewReqDto;
import com.movie.domain.review.dto.ReviewResDto;

import java.util.List;

public interface ReviewService {

    // 리뷰 추가
    ReviewResDto addReview(ReviewReqDto reviewReqDto);

    // 리뷰 삭제
    void deleteReview(Long reviewId);

    // 특정 리뷰 조회
    ReviewResDto findOneReview(Long reviewId);

    // 해당 영화 리뷰 목록
    List<ReviewResDto> findAllReviewByMovieId(Long movieId);

    //내가 작성한 리뷰 목록
    List<ReviewResDto> findMyReview();
}
