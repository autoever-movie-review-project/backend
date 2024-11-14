package com.movie.domain.movie.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.domain.movie.dao.ActorRepository;
import com.movie.domain.movie.dao.DirectorRepository;
import com.movie.domain.movie.dao.GenreRepository;
import com.movie.domain.movie.dao.MovieRepository;
import com.movie.domain.movie.domain.*;
import com.movie.domain.movie.dto.request.TmdbMovieInfo;
import com.movie.domain.movie.dto.response.TmdbMovieResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbServiceImpl implements TmdbService {
    private final RestTemplate tmdbRestTemplate;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;

    @Transactional
    @Override
    public void initializeMovies() {
        int moviesFetched = 0;
        int page = 1;
        int targetCount = 10000;

        while (moviesFetched < targetCount) {
            String url = String.format("/movie/popular?page=%d&language=ko-KR", page);
            HttpEntity<String> entity = new HttpEntity<>(new HttpHeaders());

            try {
                // 1. 인기 영화 목록 가져오기
                ResponseEntity<TmdbMovieResDto> response = tmdbRestTemplate.exchange(url, HttpMethod.GET, entity, TmdbMovieResDto.class);
                List<TmdbMovieInfo> popularMovies = response.getBody().getResults();

                if (popularMovies == null || popularMovies.isEmpty()) {
                    log.info("더 이상 인기 영화 데이터가 없습니다.");
                    break;
                }

                // 2. 각 영화의 상세 정보 및 출연진 정보 가져오기
                for (TmdbMovieInfo movieInfo : popularMovies) {
                    if (!movieRepository.existsByTmdbId(movieInfo.getId())) {
                        TmdbMovieInfo detailedMovieInfo = getMoviesWithCredits(movieInfo.getId());
                        if (detailedMovieInfo != null) {
                            String koreanAgeRating = getKoreanAgeRating(movieInfo.getId());
                            detailedMovieInfo.setAgeRating(koreanAgeRating);
                            addMovieToDb(detailedMovieInfo);
                            moviesFetched++;
                        }
                        if (moviesFetched >= targetCount) {
                            break;
                        }
                    }
                }
                page++;
            } catch (HttpClientErrorException e) {
                log.error("인기 TMDB 영화 조회 오류: 상태 코드 = {}, 응답 내용 = {}", e.getStatusCode(), e.getResponseBodyAsString());
                break;
            }
        }
        log.info("인기 영화 데이터 수집 완료 - 총 추가된 영화 개수: {}", moviesFetched);
    }

    public TmdbMovieInfo getMoviesWithCredits(Long movieId) {
        String url = String.format("/movie/%d?append_to_response=credits&language=ko-KR", movieId); // API 키 포함 필요 없음

        HttpEntity<String> entity = new HttpEntity<>(new HttpHeaders());

        try {
            ResponseEntity<TmdbMovieInfo> response = tmdbRestTemplate.exchange(url, HttpMethod.GET, entity, TmdbMovieInfo.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("TMDB 요청 오류: 상태 코드 = {}, 응답 내용 = {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        }
    }

    @Transactional
    @Override
    public String getKoreanAgeRating(Long movieId) {
        String url = String.format("/movie/%d/release_dates", movieId); // API 키 포함 필요 없음
        HttpEntity<String> entity = new HttpEntity<>(new HttpHeaders());

        try {
            ResponseEntity<String> response = tmdbRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.getBody());

            for (JsonNode resultNode : rootNode.get("results")) {
                if ("KR".equals(resultNode.get("iso_3166_1").asText())) {
                    for (JsonNode releaseDateNode : resultNode.get("release_dates")) {
                        String certification = releaseDateNode.get("certification").asText();
                        if (!certification.isEmpty()) {
                            return certification;
                        }
                    }
                }
            }
            return "등급 정보 없음"; // 한국 등급 정보가 없을 경우
        } catch (HttpClientErrorException e) {
            log.error("TMDB 요청 오류: 상태 코드 = {}, 응답 내용 = {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    @Override
    public String getPersonBirthDate(Long actorId) {
        String url = String.format("/person/%d", actorId);
        HttpEntity<String> entity = new HttpEntity<>(new HttpHeaders());

        try {
            ResponseEntity<String> response = tmdbRestTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.getBody());

            return rootNode.has("birthday") ? rootNode.get("birthday").asText() : null;
        } catch (HttpClientErrorException e) {
            log.error("배우 정보 조회 오류: 상태 코드 = {}, 응답 내용 = {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Transactional
    @Override
    public void addMovieToDb(TmdbMovieInfo movieInfo) {
        if (movieInfo == null || movieRepository.existsByTmdbId(movieInfo.getId())) {
            log.info("[중복 확인]: 영화 이미 존재하거나 TMDB 정보가 없음 - TMDB ID = {}", movieInfo.getId());
            return;
        }

        log.info("DB에 추가할 MovieInfo 데이터 id: {}", movieInfo.getId());

        Movie movie = Movie.builder()
                .tmdbId(movieInfo.getId())
                .title(movieInfo.getTitle())
                .tagline(movieInfo.getTagline())
                .plot(movieInfo.getOverview())
                .popularity(movieInfo.getPopularity())
                .backdropImg(getImageUrl(movieInfo.getBackdropPath()))
                .mainImg(getImageUrl(movieInfo.getPosterPath()))
                .releaseDate(movieInfo.getReleaseDate() != null ? LocalDate.parse(movieInfo.getReleaseDate()) : null)
                .rating(movieInfo.getVoteAverage())
                .voteCount(movieInfo.getVoteCount())
                .language(movieInfo.getOriginalLanguage())
                .ageRating(movieInfo.getAgeRating())
                .runtime(movieInfo.getRuntime())
                .build();

        if (movieInfo.getGenres() != null && !movieInfo.getGenres().isEmpty()) {
            List<MovieGenres> movieGenres = movieInfo.getGenres().stream()
                    .filter(genreDto -> genreDto != null)
                    .map(genreDto -> {
                        var genre = genreRepository.findByTmdbGenreId(genreDto.getTmdbGenreId())
                                .orElseGet(() -> genreRepository.save(
                                        Genre.builder()
                                                .tmdbGenreId(genreDto.getTmdbGenreId())
                                                .genre(genreDto.getName())
                                                .build()
                                ));
                        return MovieGenres.builder().movie(movie).genre(genre).build();
                    })
                    .collect(Collectors.toList());
            movie.addMovieGenres(movieGenres);
        }

        if (movieInfo.getCredits() != null && movieInfo.getCredits().getCrew() != null && !movieInfo.getCredits().getCrew().isEmpty()) {
            List<MovieDirectors> movieDirectors = movieInfo.getCredits().getCrew().stream()
                    .filter(directorDto -> "Director".equals(directorDto.getJob()))
                    .limit(2)
                    .map(directorDto -> {
                        String birthDate = getPersonBirthDate(directorDto.getTmdbDirectorId());
                        var director = directorRepository.findByTmdbDirectorId(directorDto.getTmdbDirectorId())
                                .orElseGet(() -> directorRepository.save(
                                        Director.builder()
                                                .tmdbDirectorId(directorDto.getTmdbDirectorId())
                                                .directorName(directorDto.getName())
                                                .birthDate(parseBirthDate(birthDate))
                                                .build()
                                ));
                        return MovieDirectors.builder().movie(movie).director(director).build();
                    })
                    .collect(Collectors.toList());
            movie.addMovieDirectors(movieDirectors);
        }

        if (movieInfo.getCredits() != null && movieInfo.getCredits().getCast() != null && !movieInfo.getCredits().getCast().isEmpty()) {
            List<MovieActors> movieActors = movieInfo.getCredits().getCast().stream()
                    .filter(actorDto -> actorDto != null && actorDto.getOrder() < 8)
                    .limit(8)
                    .map(actorDto -> {
                        String birthDate = getPersonBirthDate(actorDto.getTmdbActorId());
                        var actor = actorRepository.findByTmdbActorId(actorDto.getTmdbActorId())
                                .orElseGet(() -> actorRepository.save(
                                        Actor.builder()
                                                .tmdbActorId(actorDto.getTmdbActorId())
                                                .actorName(actorDto.getName())
                                                .birthDate(parseBirthDate(birthDate))
                                                .actorImg(actorDto.getProfilePath())
                                                .build()
                                ));
                        return MovieActors.builder().movie(movie).actor(actor).character(actorDto.getCharacter()).build();
                    })
                    .collect(Collectors.toList());
            movie.addMovieActors(movieActors);
        }

        log.info("[Movie 엔티티 저장 완료]: TMDB ID = {}", movieInfo.getId());
        movieRepository.save(movie);
    }

    @Transactional
    @Override
    public String getImageUrl(String imagePath) {
        return "https://image.tmdb.org/t/p/w500/" + imagePath;
    }

    @Transactional
    @Override
    public LocalDate parseBirthDate(String birthDate) {
        log.info("[생일 정보 파싱 시작]: 입력 값 = {}", birthDate);

        try {
            LocalDate parsedDate = birthDate != null ? LocalDate.parse(birthDate) : null;
            log.info("[생일 정보 파싱 성공]: 결과 값 = {}", parsedDate);
            return parsedDate;
        } catch (DateTimeParseException e) {
            log.warn("[생일 정보 파싱 실패]: 입력 값 = {}", birthDate);
            return null;
        }
    }


    @Scheduled(cron = "0 0 0 * * ?")
    public void updateNewMovies() {
        Long lastTmdbId = movieRepository.findMaxTmdbId().orElse(0L);
        Long currentTmdbId = lastTmdbId + 1;

        while (true) {
            TmdbMovieInfo movieInfo = getMoviesWithCredits(currentTmdbId);
            if (movieInfo != null) {
                addMovieToDb(movieInfo);
                currentTmdbId++;
            } else {
                log.info("새로운 영화 데이터가 없습니다.");
                break;
            }
        }
    }
}

