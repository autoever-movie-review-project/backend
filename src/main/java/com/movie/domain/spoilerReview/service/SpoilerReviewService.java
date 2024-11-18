package com.movie.domain.spoilerReview.service;

import com.movie.domain.likeReview.domain.LikeReview;
import com.movie.domain.likeReview.exception.LikeReviewDuplicateException;
import com.movie.domain.review.dao.ReviewRepository;
import com.movie.domain.review.domain.Review;
import com.movie.domain.spoilerReview.dao.SpoilerReviewRepository;
import com.movie.domain.spoilerReview.domain.SpoilerReview;
import com.movie.domain.spoilerReview.exception.SpoilerDuplicationException;
import com.movie.domain.user.domain.User;
import com.movie.global.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SpoilerReviewService {
    private final SpoilerReviewRepository spoilerReviewRepository;
    private final ReviewRepository reviewRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public Long save(Long reviewId) {
        // 로그인 된 유저를 가져온다.
        User loggedInUser = securityUtils.getLoginUser();

        if (spoilerReviewRepository.existsByUserUserIdAndReviewId(loggedInUser.getUserId(), reviewId)) {
            throw new SpoilerDuplicationException("스포일러 중복");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow();

        SpoilerReview spoilerReview = SpoilerReview.builder()
                .review(review)
                .user(loggedInUser)
                .build();

        spoilerReviewRepository.save(spoilerReview);

        Long countLikes = spoilerReviewRepository.countByReviewId(reviewId);

        return countLikes;
    }
}
