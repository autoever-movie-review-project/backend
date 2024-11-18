package com.movie.domain.likeReview.api;

import com.movie.domain.game.domain.Game;
import com.movie.domain.likeReview.service.LikeReviewService;
import com.movie.domain.review.domain.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class LikeReviewController {
    private final LikeReviewService likeReviewService;

    @PostMapping("/like/review/{reviewId}")
    public ResponseEntity<?> saveLike(
            @PathVariable Long reviewId
    ) {
        Long countLikes = likeReviewService.save(reviewId);

        return ResponseEntity.ok(countLikes);
    }

    @DeleteMapping("/like/{likeReviewId}/review")
    public ResponseEntity<?> deleteLike(
            @PathVariable Long likeReviewId
    ) {
        Long countLikes = likeReviewService.delete(likeReviewId);

        return ResponseEntity.ok(countLikes);
    }

    @GetMapping("/like/reviews")
    public ResponseEntity<?> getReviews(
            @RequestParam(defaultValue = "0") int page) {
        Page<Review> reviewsList = likeReviewService.getReviews(page);

        return ResponseEntity.ok(reviewsList);
    }
}
