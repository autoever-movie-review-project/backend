package com.movie.domain.movie.service;

import com.movie.domain.movie.dao.MovieRepository;
import com.movie.domain.movie.dao.TopReviewRedisRepository;
import com.movie.domain.movie.domain.Movie;
import com.movie.domain.movie.domain.TopReviewMovieInfo;
import com.movie.domain.movie.dto.response.TopReviewedMoviesResDto;
import com.movie.domain.movie.exception.MovieNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.movie.domain.movie.constant.MovieExceptionMessage.MOVIE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopReviewServiceImpl implements TopReviewService {

    private final TopReviewRedisRepository redisRepository;
    private final MovieRepository movieRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<TopReviewedMoviesResDto> findTopRevieweMovieList() {
        String redisKey = "topReviewMovieInfo";

        // 1. Redis에서 데이터 확인
        log.info("Checking if data exists in Redis with key: {}", redisKey);
        Optional<TopReviewMovieInfo> cachedReviewInfo = redisRepository.findById(redisKey);

        if (cachedReviewInfo.isPresent()) {
            TopReviewMovieInfo reviewInfo = cachedReviewInfo.get();
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);

            // 백드롭 이미지 확인 및 업데이트
            boolean isUpdated = updateMissingBackdropImagesInRedis(reviewInfo);
            if (isUpdated) {
                redisRepository.save(reviewInfo);
            }

            if (ttl != null && ttl <= 600) {
                // Redis 데이터가 곧 만료될 경우 새 데이터를 가져와 갱신
                List<TopReviewMovieInfo.MovieDetail> updatedReviews = fetchTopReviewMoviesFromDatabase();
                saveTopReviewMoviesToRedis(updatedReviews);
                return updatedReviews.stream()
                        .map(TopReviewedMoviesResDto::entityToResDto)
                        .collect(Collectors.toList());
            }

            // Redis 데이터를 API 반환용 DTO로 변환
            return reviewInfo.getReviews().stream()
                    .map(TopReviewedMoviesResDto::entityToResDto)
                    .collect(Collectors.toList());
        }

        // 2. Redis에 데이터가 없으면 DB에서 데이터 가져오기
        List<TopReviewMovieInfo.MovieDetail> fetchedReviews = fetchTopReviewMoviesFromDatabase();

        // 3. Redis에 저장
        saveTopReviewMoviesToRedis(fetchedReviews);

        // 4. API 반환용 DTO로 변환
        return fetchedReviews.stream()
                .map(TopReviewedMoviesResDto::entityToResDto)
                .collect(Collectors.toList());
    }

    private boolean updateMissingBackdropImagesInRedis(TopReviewMovieInfo reviewInfo) {
        boolean isUpdated = false;

        for (TopReviewMovieInfo.MovieDetail detail : reviewInfo.getReviews()) {
            if (detail.getBackdropImg() == null) {
                log.info("Backdrop image missing for movieId: {}", detail.getMovieId());

                // DB에서 백드롭 이미지 가져오기
                Movie movie = movieRepository.findById(detail.getMovieId())
                        .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND.getMessage()));

                String backdropImg = movie.getBackdropImg();
                if (backdropImg != null) {
                    log.info("Updating backdrop image for movieId: {}", detail.getMovieId());
                    detail.updateBackdropImg(backdropImg);
                    isUpdated = true;
                }
            }
        }

        return isUpdated;
    }

    private List<TopReviewMovieInfo.MovieDetail> fetchTopReviewMoviesFromDatabase() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);

        List<Object[]> reviewCounts = movieRepository.findTopMoviesByReviewCount(startDate, endDate, 10);

        // DB에서 가져온 데이터를 Redis 저장용 MovieDetail로 변환
        return IntStream.range(0, reviewCounts.size())
                .mapToObj(index -> {
                    Object[] data = reviewCounts.get(index);
                    Long movieId = ((BigInteger) data[0]).longValue();
                    Long reviewCount = ((BigInteger) data[1]).longValue();

                    Movie movie = movieRepository.findById(movieId)
                            .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND.getMessage()));

                    // MovieDetail 생성
                    return TopReviewMovieInfo.MovieDetail.builder()
                            .movieId(movieId)
                            .rank(String.valueOf(index + 1))
                            .reviewCount(reviewCount)
                            .mainImg(movie.getMainImg())
                            .backdropImg(movie.getBackdropImg())
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

    private void saveTopReviewMoviesToRedis(List<TopReviewMovieInfo.MovieDetail> reviews) {
        TopReviewMovieInfo reviewInfo = TopReviewMovieInfo.builder()
                .targetDate(LocalDateTime.now().toString())
                .reviews(reviews)
                .expiration(1)
                .build();

        redisRepository.save(reviewInfo);
    }
}
