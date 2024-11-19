package com.movie.domain.movie.service;

import com.movie.domain.movie.dao.MovieRepository;
import com.movie.domain.movie.dao.TopReviewRedisRepository;
import com.movie.domain.movie.domain.Movie;
import com.movie.domain.movie.domain.TopReviewMovieInfo;
import com.movie.domain.movie.dto.response.TopReviewedMoviesResDto;
import com.movie.domain.movie.exception.MovieNotFoundException;
import lombok.RequiredArgsConstructor;
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
        Optional<TopReviewMovieInfo> cachedReviewInfo = redisRepository.findById(redisKey);

        if (cachedReviewInfo.isPresent()) {
            TopReviewMovieInfo reviewInfo = cachedReviewInfo.get();
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);

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

    // 제목 내 로마 숫자와 아라비아 숫자를 동일하게 취급
    private String normalizeTitle(String title) {
        // 로마 숫자와 아라비아 숫자 변환
        title = title.replace("Ⅱ", "2");
        title = title.replace("Ⅲ", "3");
        title = title.replace("Ⅳ", "4");
        title = title.replace("Ⅴ", "5");
        title = title.replace("Ⅵ", "6");
        title = title.replace("Ⅶ", "7");
        title = title.replace("Ⅷ", "8");
        title = title.replace("Ⅸ", "9");
        title = title.replace("Ⅹ", "10");
        return title;
    }
}
