package com.movie.domain.likeReview.api;

import com.movie.domain.likeReview.service.LikeReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
