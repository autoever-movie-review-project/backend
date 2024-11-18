package com.movie.domain.review.service;

import com.movie.domain.movie.dao.MovieRepository;
import com.movie.domain.movie.domain.Movie;
import com.movie.domain.movie.exception.MovieNotFoundException;
import com.movie.domain.review.constant.ReviewExceptionMessage;
import com.movie.domain.review.dao.ReviewRepository;
import com.movie.domain.review.domain.Review;
import com.movie.domain.review.dto.ReviewReqDto;
import com.movie.domain.review.dto.ReviewResDto;
import com.movie.domain.review.exception.ReviewNotFoundException;
import com.movie.domain.user.dao.UserRepository;
import com.movie.domain.user.domain.User;
import com.movie.global.exception.ForbiddenException;
import com.movie.global.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final SecurityUtils securityUtils;

    private User getLoginUser() {
        String loginUserEmail = securityUtils.getLoginUserEmail();
        return userRepository.findByEmail(loginUserEmail)
                .orElseThrow(() -> new ForbiddenException(ReviewExceptionMessage.NO_PERMISSION.getMessage()));
    }

    @Transactional
    @Override
    public ReviewResDto addReview(ReviewReqDto reviewReqDto) {
        User writer = getLoginUser();
        Movie movie = movieRepository.findById(reviewReqDto.getMovieId())
                .orElseThrow(() -> new MovieNotFoundException(ReviewExceptionMessage.REVIEW_MOVIE_NOT_FOUND.getMessage()));

        Review review = Review.builder()
                .user(writer)
                .movie(movie)
                .content(reviewReqDto.getContent())
                .rating(reviewReqDto.getRating())
                .build();

        reviewRepository.save(review);
        return ReviewResDto.entityToResDto(review);
    }

    @Transactional
    @Override
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(ReviewExceptionMessage.REVIEW_NOT_FOUND.getMessage()));

        User loginUser = getLoginUser();
        if (!review.getUser().equals(loginUser)) {
            throw new ForbiddenException(ReviewExceptionMessage.NO_PERMISSION.getMessage());
        }

        reviewRepository.delete(review);
    }

    @Transactional
    @Override
    public ReviewResDto findOneReview(Long reviewId) {
        Review review = reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(ReviewExceptionMessage.REVIEW_NOT_FOUND.getMessage()));
        return ReviewResDto.entityToResDto(review);
    }

    @Override
    public List<ReviewResDto> findAllReviewByMovieId(Long movieId) {
        List<Review> reviews = reviewRepository.findAllByMovieId(movieId);
        return reviews.stream()
                .map(ReviewResDto::entityToResDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<ReviewResDto> findMyReview() {
        User loginUser = getLoginUser();
        List<Review> myReviews = reviewRepository.findAllByUserId(loginUser.getUserId());
        return myReviews.stream()
                .map(ReviewResDto::entityToResDto)
                .collect(Collectors.toList());
    }
}
