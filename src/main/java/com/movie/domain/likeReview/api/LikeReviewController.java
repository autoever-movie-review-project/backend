package com.movie.domain.likeReview.api;

import com.movie.domain.likeReview.service.LikeReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class LikeReviewController {
    private final LikeReviewService likeReviewService;


}
