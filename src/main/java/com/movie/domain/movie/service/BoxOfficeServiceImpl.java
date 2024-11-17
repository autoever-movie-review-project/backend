package com.movie.domain.movie.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.movie.domain.movie.domain.BoxOfficeMovieInfo;
import com.movie.domain.movie.dao.BoxOfficeRedisRepository;
import com.movie.domain.movie.dao.MovieRepository;
import com.movie.domain.movie.domain.Movie;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.movie.domain.movie.constant.MovieExceptionMessage.BOX_OFFICE_DATA_FETCH_FAILED;

@Service
@RequiredArgsConstructor
public class BoxOfficeServiceImpl implements BoxOfficeService {

    private final BoxOfficeRedisRepository redisRepository;
    private final MovieRepository movieRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate boxOfficeRestTemplate;

    // 현재 상영 중인 영화 -> 박스오피스 기준
    @Override
    public List<BoxOfficeMovieInfo.MovieDetail> getDailyBoxOfficeList(String targetDate) {
        if (targetDate == null || targetDate.isBlank()) {
            throw new IllegalArgumentException("Target date is required.");
        }

        // 1. Redis에서 데이터 확인
        Optional<BoxOfficeMovieInfo> cachedBoxOfficeInfo = redisRepository.findById(targetDate);

        if (cachedBoxOfficeInfo.isPresent()) {
            BoxOfficeMovieInfo boxOfficeInfo = cachedBoxOfficeInfo.get();
            Long ttl = redisTemplate.getExpire("boxOfficeMovieInfo:" + targetDate, TimeUnit.SECONDS);

            // TTL이 600초(10분) 이하로 남은 경우, 데이터를 갱신
            if (ttl != null && ttl <= 600) {
                List<BoxOfficeMovieInfo.MovieDetail> updatedMovies = fetchBoxOfficeFromApi(targetDate);
                saveBoxOfficeToRedis(targetDate, updatedMovies);
                return updatedMovies;
            }
            return boxOfficeInfo.getMovies();
        }

        // 2. Redis에 데이터가 없으면 영화진흥원 API 호출
        List<BoxOfficeMovieInfo.MovieDetail> fetchedMovies = fetchBoxOfficeFromApi(targetDate);

        // 3. Redis에 저장
        saveBoxOfficeToRedis(targetDate, fetchedMovies);

        return fetchedMovies;
    }

    // 영화진흥원 API를 통해 데이터 가져오기
    private List<BoxOfficeMovieInfo.MovieDetail> fetchBoxOfficeFromApi(String targetDate) {
        String endpoint = "/searchDailyBoxOfficeList.json?targetDt=" + targetDate;
        JsonNode response = boxOfficeRestTemplate.getForObject(endpoint, JsonNode.class);

        if (response == null || !response.has("boxOfficeResult")) {
            throw new RuntimeException(BOX_OFFICE_DATA_FETCH_FAILED.getMessage());
        }

        JsonNode dailyBoxOfficeList = response.path("boxOfficeResult").path("dailyBoxOfficeList");
        List<BoxOfficeMovieInfo.MovieDetail> movieDetails = new ArrayList<>();

        LocalDate parsedDate = LocalDate.parse(targetDate, DateTimeFormatter.ofPattern("yyyyMMdd"));

        for (JsonNode node : dailyBoxOfficeList) {
            String title = node.get("movieNm").asText();
            String rank = node.get("rank").asText();
            String audience = node.get("audiCnt").asText();

            Optional<Movie> movieOpt = movieRepository.findByTitleAndReleaseDate(title, parsedDate);

            if (movieOpt.isPresent()) {
                Movie movie = movieOpt.get();
                movieDetails.add(BoxOfficeMovieInfo.MovieDetail.builder()
                        .movieId(movie.getMovieId())
                        .rank(rank)
                        .audience(audience)
                        .mainImg(movie.getMainImg())
                        .rating(movie.getRating())
                        .title(movie.getTitle())
                        .genres(movie.getMovieGenres().stream()
                                .map(movieGenre -> movieGenre.getGenre().getGenre())
                                .collect(Collectors.toList()))
                        .ageRating(movie.getAgeRating())
                        .tagline(movie.getTagline())
                        .build());
            }
        }

        // 순위 정렬 및 업데이트
        movieDetails.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getRank())));
        for (int i = 0; i < movieDetails.size(); i++) {
            movieDetails.get(i).updateRank(String.valueOf(i + 1));
        }

        return movieDetails;
    }

    // Redis에 데이터를 저장
    private void saveBoxOfficeToRedis(String targetDate, List<BoxOfficeMovieInfo.MovieDetail> movies) {
        BoxOfficeMovieInfo boxOfficeInfo = BoxOfficeMovieInfo.builder()
                .targetDate(targetDate)
                .movies(movies)
                .expiration(1) // TTL: 1일
                .build();

        redisRepository.save(boxOfficeInfo);
    }
}
