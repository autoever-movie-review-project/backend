package com.movie.domain.movie.service;

import com.movie.domain.movie.dao.TopReviewRedisRepository;
import com.movie.domain.movie.dao.MovieRepository;
import com.movie.domain.movie.domain.Movie;
import com.movie.domain.movie.domain.TopReviewMovieInfo;
import com.movie.domain.movie.exception.MovieNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.movie.domain.movie.constant.MovieExceptionMessage.MOVIE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TopReviewServiceImpl implements TopReviewService {

    private final TopReviewRedisRepository redisRepository;
    private final MovieRepository movieRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // 일주일간 리뷰 기준 상위 10개 영화 반환
    @Override
    public List<TopReviewMovieInfo.MovieDetail> getTopRevieweMovieList() {
        String redisKey = "topReviewMovieInfo";

        // 1. Redis에서 데이터 확인
        Optional<TopReviewMovieInfo> cachedReviewInfo = redisRepository.findById(redisKey);

        if (cachedReviewInfo.isPresent()) {
            TopReviewMovieInfo reviewInfo = cachedReviewInfo.get();
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);

            if (ttl != null && ttl <= 600) {
                List<TopReviewMovieInfo.MovieDetail> updatedReviews = fetchTopReviewMoviesFromDatabase();
                saveTopReviewMoviesToRedis(updatedReviews);
                return updatedReviews;
            }
            return reviewInfo.getReviews();
        }

        // 2. Redis에 데이터가 없으면 DB에서 데이터 가져오기
        List<TopReviewMovieInfo.MovieDetail> fetchedReviews = fetchTopReviewMoviesFromDatabase();

        // 3. Redis에 저장
        saveTopReviewMoviesToRedis(fetchedReviews);

        return fetchedReviews;
    }

    private List<TopReviewMovieInfo.MovieDetail> fetchTopReviewMoviesFromDatabase() {
        // 1. 최근 일주일 리뷰 데이터 가져오기
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);

        List<Object[]> reviewCounts = movieRepository.findTopMoviesByReviewCount(startDate, endDate, 10);

        // 2. 리뷰 데이터를 기반으로 영화 정보를 생성
        return IntStream.range(0, reviewCounts.size())
                .mapToObj(index -> {
                    Object[] data = reviewCounts.get(index);
                    Long movieId = (Long) data[0];
                    Long reviewCount = (Long) data[1];

                    Movie movie = movieRepository.findById(movieId)
                            .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND.getMessage()));

                    return TopReviewMovieInfo.MovieDetail.builder()
                            .movieId(movieId)
                            .rank(String.valueOf(index + 1))
                            .reviewCount(reviewCount)
                            .mainImg(movie.getMainImg())
                            .rating(movie.getRating())
                            .title(movie.getTitle())
                            .genres(movie.getMovieGenres().stream()
                                    .map(genre -> genre.getGenre().getGenre())
                                    .collect(Collectors.toList()))
                            .ageRating(movie.getAgeRating())
                            .tagline(movie.getTagline())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // Redis에 데이터를 저장
    private void saveTopReviewMoviesToRedis(List<TopReviewMovieInfo.MovieDetail> reviews) {
        TopReviewMovieInfo reviewInfo = TopReviewMovieInfo.builder()
                .targetDate(LocalDateTime.now().toString())
                .reviews(reviews)
                .expiration(1)
                .build();

        redisRepository.save(reviewInfo);
    }
}
