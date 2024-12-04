package com.movie.domain.spoilerReview.api;

import com.movie.domain.spoilerReview.service.SpoilerReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SpoilerReviewController {
    private final SpoilerReviewService spoilerReviewService;

    @PostMapping("/spoiler/{reviewId}")
    public ResponseEntity<?> saveSpoilerReview(@PathVariable Long reviewId) {
        Long countSpoiler = spoilerReviewService.save(reviewId);

        return ResponseEntity.ok(countSpoiler);
    }
}
