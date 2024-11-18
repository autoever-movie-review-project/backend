package com.movie.domain.likeReview.service;

import com.movie.domain.review.dao.ReviewRepository;
import com.movie.domain.review.domain.Review;
import com.movie.domain.likeReview.dao.LikeReviewRepository;
import com.movie.domain.likeReview.domain.LikeReview;
import com.movie.domain.likeReview.exception.LikeReviewDuplicateException;
import com.movie.domain.user.domain.User;
import com.movie.global.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

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

        if (likeReviewRepository.existsByUserUserIdAndReviewId(loggedInUser.getUserId(), reviewId)) {
            throw new LikeReviewDuplicateException("좋아요 중복");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow();

        LikeReview likeReview = LikeReview.builder()
                .review(review)
                .user(loggedInUser)
                .build();

        likeReviewRepository.save(likeReview);

        Long countLikes = likeReviewRepository.countByReviewId(reviewId);

        return countLikes;

    }

    public Long delete(Long reviewId) {
        // 1) 현재 로그인 된 멤버의 ID를 가져온다.
        User loggedInUser = securityUtils.getLoginUser();

        LikeReview like = likeReviewRepository.findByUserUserIdAndReviewId(loggedInUser.getUserId(), reviewId);

        likeReviewRepository.delete(like);

        Long countLikes = likeReviewRepository.countByReviewId(reviewId);

        return countLikes;
    }

    public Page<Review> getReviews(int page) {

        // 1현재 로그인 된 멤버의 ID를 가져온다.
        User loggedInUser = securityUtils.getLoginUser();

        //  pageable 인터페이스 생성
        Pageable pageable = PageRequest.of(page, 9, Sort.by(Sort.Direction.DESC, "id"));

        List<LikeReview> likeReviews = likeReviewRepository.findAllByUserUserId(loggedInUser.getUserId());


        // 4. likeReviews에서 reviewId 추출
        List<Long> reviewIds = likeReviews.stream()
                .map(likeReview -> likeReview.getReview().getId())
                .toList();

        // 5. reviewId 리스트를 사용해 Review 데이터 페이징 조회
        return reviewRepository.findByIdIn(reviewIds, pageable);

    }
}