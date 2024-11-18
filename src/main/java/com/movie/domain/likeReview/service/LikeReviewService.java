package com.movie.domain.likeReview.service;

import com.movie.domain.likeReview.dto.response.ReviewDto;
import com.movie.domain.review.dao.ReviewRepository;
import com.movie.domain.review.domain.Review;
import com.movie.domain.likeReview.dao.LikeReviewRepository;
import com.movie.domain.likeReview.domain.LikeReview;
import com.movie.domain.likeReview.exception.LikeReviewDuplicateException;
import com.movie.domain.review.exception.ReviewNotFoundException;
import com.movie.domain.user.domain.User;
import com.movie.global.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

import static com.movie.domain.review.constant.ReviewExceptionMessage.REVIEW_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class LikeReviewService {
    private final LikeReviewRepository likeReviewRepository;
    private final SecurityUtils securityUtils;
    private final ReviewRepository reviewRepository;

    @Transactional
    public Long save(Long reviewId) {
        // 로그인 된 유저를 가져온다.
        User loggedInUser = securityUtils.getLoginUser();

        if (likeReviewRepository.existsByUserUserIdAndReviewReviewId(loggedInUser.getUserId(), reviewId)) {
            throw new LikeReviewDuplicateException("좋아요 중복");
        }

        Review review = reviewRepository.findByReviewId(reviewId)
                .orElseThrow(()->new ReviewNotFoundException(REVIEW_NOT_FOUND.getMessage()));

        review.upLikeCount();

        LikeReview likeReview = LikeReview.builder()
                .review(review)
                .user(loggedInUser)
                .build();

        reviewRepository.save(review);

        likeReviewRepository.save(likeReview);

        Long countLikes = likeReviewRepository.countByReviewReviewId(reviewId);

        return countLikes;

    }

    public Long delete(Long reviewId) {
        // 1) 현재 로그인 된 멤버의 ID를 가져온다.
        User loggedInUser = securityUtils.getLoginUser();

        LikeReview like = likeReviewRepository.findByUserUserIdAndReviewReviewId(loggedInUser.getUserId(), reviewId);

        likeReviewRepository.delete(like);

        Long countLikes = likeReviewRepository.countByReviewReviewId(reviewId);

        return countLikes;
    }

    public Page<ReviewDto> getReviews(int page) {

        // 1. 현재 로그인된 유저 가져오기
        User loggedInUser = securityUtils.getLoginUser();

        // 2. Pageable 설정
        Pageable pageable = PageRequest.of(page, 9, Sort.by(Sort.Direction.DESC, "reviewId"));

        // 3. 유저가 좋아요한 리뷰 목록 가져오기
        List<LikeReview> likeReviews = likeReviewRepository.findAllByUserUserId(loggedInUser.getUserId());

        // 4. 좋아요한 리뷰의 ID 리스트 생성
        List<Long> reviewIds = likeReviews.stream()
                .map(likeReview -> likeReview.getReview().getReviewId())
                .toList();

        // 5. 좋아요한 리뷰를 페이징 조회
        Page<Review> reviewsPage = reviewRepository.findByReviewIdIn(reviewIds, pageable);

        // 6. Review 객체를 ReviewDto로 변환
        List<ReviewDto> reviewDtos = reviewsPage.getContent().stream()
                .map(ReviewDto::of)
                .toList();

        // 7. ReviewDto 리스트를 Page 객체로 반환
        return new PageImpl<>(reviewDtos, pageable, reviewsPage.getTotalElements());
    }

}