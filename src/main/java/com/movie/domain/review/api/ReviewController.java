package com.movie.domain.review.api;

import com.movie.domain.review.dto.ReviewReqDto;
import com.movie.domain.review.dto.ReviewResDto;
import com.movie.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/review")
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 등록
    @PostMapping("")
    public ResponseEntity<ReviewResDto> addReview(@RequestBody ReviewReqDto reviewReqDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.addReview(reviewReqDto));
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 특정 리뷰 조회
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResDto> findReview(@PathVariable Long reviewId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(reviewService.findOneReview(reviewId));
    }

    // 한 영화에 대한 리뷰 조회
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ReviewResDto>> findAllReviewsByMovieId(@PathVariable Long movieId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(reviewService.findAllReviewByMovieId(movieId));
    }

    // 내가 작성한 모든 리뷰 조회
    @GetMapping("/my")
    public ResponseEntity<List<ReviewResDto>> findAllReviewsByUserId() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(reviewService.findMyReview());
    }
}
