package com.movie.domain.movie.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.movie.domain.movie.dao.BoxOfficeRedisRepository;
import com.movie.domain.movie.dao.MovieRepository;
import com.movie.domain.movie.domain.BoxOfficeMovieInfo;
import com.movie.domain.movie.domain.Movie;
import com.movie.domain.movie.dto.response.BoxOfficeListResDto;
import com.movie.global.config.BoxOfficeConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoxOfficeServiceImpl implements BoxOfficeService {

    private final BoxOfficeRedisRepository redisRepository;
    private final MovieRepository movieRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BoxOfficeConfig boxOfficeConfig;
    private final RestTemplate boxOfficeRestTemplate;

    @Transactional
    @Override
    public List<BoxOfficeListResDto> findDailyBoxOfficeList() {
        String targetDate = getDefaultTargetDate();

        // 1. Redis에서 데이터 확인
        Optional<BoxOfficeMovieInfo> cachedBoxOfficeInfo = redisRepository.findById(targetDate);
        if (cachedBoxOfficeInfo.isPresent()) {
            BoxOfficeMovieInfo boxOfficeInfo = cachedBoxOfficeInfo.get();
            Long ttl = redisTemplate.getExpire("boxOfficeMovieInfo:" + targetDate, TimeUnit.SECONDS);

            if (ttl != null && ttl <= 600) {
                log.info("TTL이 600초 이하이므로 데이터를 갱신합니다.");
                List<BoxOfficeMovieInfo.MovieDetail> updatedMovies = fetchBoxOfficeFromApi(targetDate);
                saveBoxOfficeToRedis(targetDate, updatedMovies);
                return updatedMovies.stream()
                        .map(BoxOfficeListResDto::entityToResDto)
                        .collect(Collectors.toList());
            }
            return boxOfficeInfo.getMovies().stream()
                    .map(BoxOfficeListResDto::entityToResDto)
                    .collect(Collectors.toList());
        }

        log.info("Redis 캐시에서 데이터를 찾을 수 없어 API를 호출합니다.");

        // 2. Redis에 데이터가 없으면 영화진흥원 API 호출
        List<BoxOfficeMovieInfo.MovieDetail> fetchedMovies = fetchBoxOfficeFromApi(targetDate);

        // 3. Redis에 저장
        saveBoxOfficeToRedis(targetDate, fetchedMovies);

        // 4. API 반환용 DTO로 변환
        return fetchedMovies.stream()
                .map(BoxOfficeListResDto::entityToResDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<BoxOfficeMovieInfo.MovieDetail> fetchBoxOfficeFromApi(String targetDate) {
        String endpoint = "?key=" + boxOfficeConfig.getApiKey() + "&targetDt=" + targetDate;

        String fullUrl = boxOfficeRestTemplate.getUriTemplateHandler().expand(endpoint).toString();
        log.info("박스오피스 API 호출 URL: {}", fullUrl);

        JsonNode response;
        try {
            response = boxOfficeRestTemplate.getForObject(endpoint, JsonNode.class);
        } catch (HttpClientErrorException.NotFound e) {
            log.error("박스오피스 API 호출 중 404 오류 발생. URL: {}", fullUrl);
            throw new RuntimeException("해당 날짜의 박스오피스 데이터를 찾을 수 없습니다.");
        }

        if (response == null || !response.has("boxOfficeResult")) {
            log.warn("박스오피스 데이터를 가져올 수 없습니다: boxOfficeResult 없음");
            return new ArrayList<>();
        }

        JsonNode dailyBoxOfficeList = response.path("boxOfficeResult").path("dailyBoxOfficeList");

        if (dailyBoxOfficeList.isEmpty()) {
            log.warn("박스오피스 응답 dailyBoxOfficeList가 비어 있습니다.");
            return new ArrayList<>();
        }

        return parseMovies(dailyBoxOfficeList);
    }

    @Transactional
    public List<BoxOfficeMovieInfo.MovieDetail> parseMovies(JsonNode dailyBoxOfficeList) {
        List<BoxOfficeMovieInfo.MovieDetail> movieDetails = new ArrayList<>();

        for (JsonNode node : dailyBoxOfficeList) {
            String title = node.get("movieNm").asText().trim();
            title = normalizeTitle(title);

            String rank = node.get("rank").asText();
            String audience = node.get("audiCnt").asText();
            String openDate = node.get("openDt").asText();
            LocalDate releaseDate = LocalDate.parse(openDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate startDate = releaseDate.minusMonths(1);
            LocalDate endDate = releaseDate.plusMonths(1);

            Optional<Movie> movieOpt = movieRepository.findByTitleAndReleaseDateRange(title, startDate, endDate);
            if (movieOpt.isPresent()) {
                Movie movie = movieOpt.get();
                List<String> genres = movie.getMovieGenres().stream()
                        .map(movieGenre -> movieGenre.getGenre().getGenre())
                        .collect(Collectors.toList());
                movieDetails.add(BoxOfficeMovieInfo.MovieDetail.builder()
                        .movieId(movie.getMovieId())
                        .rank(rank)
                        .audience(audience)
                        .mainImg(movie.getMainImg())
                        .rating(movie.getRating())
                        .title(movie.getTitle())
                        .genres(genres)
                        .ageRating(movie.getAgeRating())
                        .tagline(movie.getTagline())
                        .build());
            }
        }

        movieDetails.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getRank())));

        // rank 업데이트
        for (int i = 0; i < movieDetails.size(); i++) {
            movieDetails.get(i).updateRank(String.valueOf(i + 1));
        }

        return movieDetails;
    }

    @Transactional
    public void saveBoxOfficeToRedis(String targetDate, List<BoxOfficeMovieInfo.MovieDetail> movies) {
        if (movies == null || movies.isEmpty()) {
            log.info("저장할 박스오피스 데이터가 없습니다.");
            return;
        }

        BoxOfficeMovieInfo boxOfficeInfo = BoxOfficeMovieInfo.builder()
                .targetDate(targetDate)
                .movies(movies)
                .expiration(1) // TTL: 1일
                .build();

        if (boxOfficeInfo == null) {
            log.warn("BoxOfficeMovieInfo 객체가 null입니다.");
            return;
        }

        redisRepository.save(boxOfficeInfo);
    }

    private String getDefaultTargetDate() {
        return LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    // 아라비아 숫자 처리 함수
    private String normalizeTitle(String title) {
        // "Ⅱ", "Ⅲ" 등 아라비아 숫자를 "II", "III"로 변환
        title = title.replaceAll("Ⅱ", "II")
                .replaceAll("Ⅲ", "III")
                .replaceAll("Ⅳ", "IV")
                .replaceAll("Ⅴ", "V")
                .replaceAll("Ⅵ", "VI")
                .replaceAll("Ⅶ", "VII")
                .replaceAll("Ⅷ", "VIII")
                .replaceAll("Ⅸ", "IX")
                .replaceAll("Ⅹ", "X");

        return title;
    }
}
