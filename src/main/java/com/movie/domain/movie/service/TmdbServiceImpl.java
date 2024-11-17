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
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.movie.domain.movie.constant.MovieExceptionMessage.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbServiceImpl implements TmdbService {
    private final RestTemplate tmdbRestTemplate;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;

    private static final int BATCH_SIZE = 20;

    @Override
    public void initializeMovies() {
        int moviesFetched = 0;
        int page = 1;
        int targetCount = 5000;

        List<Movie> moviesToSave = new ArrayList<>();

        while (moviesFetched < targetCount) {
            String url = String.format("/movie/popular?page=%d&language=ko-KR", page);

            try {
                log.info("[페이지 시작] 페이지 번호: {}", page);

                ResponseEntity<TmdbMovieResDto> response = tmdbRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, TmdbMovieResDto.class);
                List<TmdbMovieInfo> popularMovies = response.getBody().getResults();

                if (popularMovies == null || popularMovies.isEmpty()) {
                    log.info("[데이터 없음] 페이지: {}", page);
                    break;
                }

                List<Long> existingTmdbIds = movieRepository.findTmdbIds(popularMovies.stream()
                        .map(TmdbMovieInfo::getId)
                        .collect(Collectors.toList()));

                for (TmdbMovieInfo movieInfo : popularMovies) {
                    if (moviesFetched >= targetCount) break;

                    if (existingTmdbIds.contains(movieInfo.getId())) {
                        log.info("[중복 데이터 건너뜀] TMDB ID: {}", movieInfo.getId());
                        continue;
                    }

                    TmdbMovieInfo detailedMovieInfo = getMoviesWithCredits(movieInfo.getId());
                    if (detailedMovieInfo == null || !isValidMovie(detailedMovieInfo)) {
                        continue;
                    }

                    Movie movie = createMovieEntity(detailedMovieInfo);
                    if (movie != null) {
                        moviesToSave.add(movie);
                        moviesFetched++;
                    }

                    if (moviesToSave.size() >= BATCH_SIZE) {
                        saveMoviesInTransaction(moviesToSave);
                        moviesToSave.clear();
                    }
                }

                page++;

            } catch (Exception e) {
                log.error("[TMDB API 오류] {}", e.getMessage());
                break;
            }
        }

        if (!moviesToSave.isEmpty()) {
            saveMoviesInTransaction(moviesToSave);
        }

        log.info("[작업 완료] 총 저장된 영화 수: {}", moviesFetched);
    }


    //특정 영화 db에 직접 넣기
    @Transactional
    @Override
    public void searchAndSaveMovie(Long tmdbId) {
        // TMDB ID로 상세 정보 가져오기
        try {
            log.info("Fetching movie details for TMDB ID: {}", tmdbId);

            // TMDB API 호출하여 상세 정보 가져오기
            TmdbMovieInfo detailedMovieInfo = getMoviesWithCredits(tmdbId);
            if (detailedMovieInfo == null || !isValidMovie(detailedMovieInfo)) {
                throw new RuntimeException(MOVIE_NOT_FOUND.getMessage());
            }

            // DB에 존재하는지 확인
            boolean existsInDatabase = movieRepository.existsByTitleAndReleaseDate(
                    detailedMovieInfo.getTitle(),
                    parseDate(detailedMovieInfo.getReleaseDate())
            );
            if (existsInDatabase) {
                log.info("영화가 이미 데이터베이스에 존재합니다. 제목: {}, 개봉일: {}",
                        detailedMovieInfo.getTitle(),
                        detailedMovieInfo.getReleaseDate());
                return;
            }

            // 영화 저장
            Movie movie = createMovieEntity(detailedMovieInfo);
            if (movie == null) {
                throw new RuntimeException(MOVIE_SAVE_ERROR.getMessage());
            }
            movieRepository.save(movie);

            log.info("영화가 성공적으로 저장되었습니다. 제목: {}, 개봉일: {}",
                    detailedMovieInfo.getTitle(),
                    detailedMovieInfo.getReleaseDate());

        } catch (Exception e) {
            log.error("[TMDB API 오류] TMDB ID: {}, 오류: {}", tmdbId, e.getMessage());
            throw new RuntimeException(TMDB_API_CALL_FAILED.getMessage());
        }
    }


    //    @Scheduled(cron = "0 0 0 * * ?")
    public void updateNewMovies() {

    }

    private boolean isValidMovie(TmdbMovieInfo movieInfo) {
        if (movieInfo.getTagline() != null && movieInfo.getTagline().length() > 255) {
            log.warn("[건너뜀] 태그라인 길이 초과 - TMDB ID: {}", movieInfo.getId());
            return false;
        }

        if (movieInfo.getCredits() != null && movieInfo.getCredits().getCast() != null &&
                movieInfo.getCredits().getCast().stream()
                        .anyMatch(actor -> actor.getCharacter() != null && actor.getCharacter().length() > 255)) {
            log.warn("[건너뜀] 배역 이름 길이 초과 - TMDB ID: {}", movieInfo.getId());
            return false;
        }

        return true;
    }

    private TmdbMovieInfo getMoviesWithCredits(Long movieId) {
        String url = String.format("/movie/%d?append_to_response=credits&language=ko-KR", movieId);

        try {
            ResponseEntity<TmdbMovieInfo> response = tmdbRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, TmdbMovieInfo.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("[TMDB 요청 실패] TMDB ID: {}, 오류: {}", movieId, e.getMessage());
            return null;
        }
    }

    private String getKoreanAgeRating(Long movieId) {
        String url = String.format("/movie/%d/release_dates", movieId);

        try {
            ResponseEntity<String> response = tmdbRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, String.class);
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

            return "등급 정보 없음";

        } catch (Exception e) {
            log.error("[등급 조회 실패] TMDB ID: {}, 오류: {}", movieId, e.getMessage());
            return null;
        }
    }

    private String getPersonBirthDate(Long personId) {
        String url = String.format("/person/%d?language=ko-KR", personId);

        try {
            ResponseEntity<String> response = tmdbRestTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.getBody());
            return rootNode.has("birthday") ? rootNode.get("birthday").asText() : null;
        } catch (Exception e) {
            log.error("[생일 조회 실패] TMDB Person ID: {}, 오류: {}", personId, e.getMessage());
            return null;
        }
    }


    private Movie createMovieEntity(TmdbMovieInfo movieInfo) {
        if (movieInfo == null) return null;

        Movie movie = Movie.builder()
                .tmdbId(movieInfo.getId())
                .title(movieInfo.getTitle())
                .tagline(movieInfo.getTagline())
                .plot(movieInfo.getOverview())
                .popularity(movieInfo.getPopularity())
                .backdropImg(getImageUrl(movieInfo.getBackdropPath()))
                .mainImg(getImageUrl(movieInfo.getPosterPath()))
                .releaseDate(parseDate(movieInfo.getReleaseDate()))
                .rating(movieInfo.getVoteAverage())
                .voteCount(movieInfo.getVoteCount())
                .language(movieInfo.getOriginalLanguage())
                .ageRating(getKoreanAgeRating(movieInfo.getId()))
                .runtime(movieInfo.getRuntime())
                .reviewCount(0)
                .build();

        // 장르, 감독, 배우 설정
        setMovieRelations(movie, movieInfo);

        return movie;
    }

    private void setMovieRelations(Movie movie, TmdbMovieInfo movieInfo) {
        // 장르 설정
        if (movieInfo.getGenres() != null && !movieInfo.getGenres().isEmpty()) {
            List<MovieGenres> movieGenres = movieInfo.getGenres().stream()
                    .map(genreDto -> {
                        Genre genre = genreRepository.findByTmdbGenreId(genreDto.getTmdbGenreId())
                                .orElseGet(() -> genreRepository.save(Genre.builder()
                                        .tmdbGenreId(genreDto.getTmdbGenreId())
                                        .genre(genreDto.getName())
                                        .build()));
                        return MovieGenres.builder().movie(movie).genre(genre).build();
                    }).collect(Collectors.toList());
            movie.addMovieGenres(movieGenres);
        }

        // 감독 설정
        if (movieInfo.getCredits() != null && movieInfo.getCredits().getCrew() != null) {
            List<MovieDirectors> movieDirectors = movieInfo.getCredits().getCrew().stream()
                    .filter(directorDto -> "Director".equals(directorDto.getJob()))
                    .limit(2)
                    .map(directorDto -> {
                        String birthDate = getPersonBirthDate(directorDto.getTmdbDirectorId());
                        Director director = directorRepository.findByTmdbDirectorId(directorDto.getTmdbDirectorId())
                                .orElseGet(() -> directorRepository.save(Director.builder()
                                        .tmdbDirectorId(directorDto.getTmdbDirectorId())
                                        .directorName(directorDto.getName())
                                        .birthDate(parseDate(birthDate))
                                        .build()));
                        return MovieDirectors.builder().movie(movie).director(director).build();
                    }).collect(Collectors.toList());
            movie.addMovieDirectors(movieDirectors);
        }


        // 배우 설정
        if (movieInfo.getCredits() != null && movieInfo.getCredits().getCast() != null) {
            List<MovieActors> movieActors = movieInfo.getCredits().getCast().stream()
                    .filter(actorDto -> actorDto != null && actorDto.getOrder() < 8)
                    .limit(8)
                    .map(actorDto -> {
                        String birthDate = getPersonBirthDate(actorDto.getTmdbActorId());
                        Actor actor = actorRepository.findByTmdbActorId(actorDto.getTmdbActorId())
                                .orElseGet(() -> actorRepository.save(Actor.builder()
                                        .tmdbActorId(actorDto.getTmdbActorId())
                                        .actorName(actorDto.getName())
                                        .birthDate(parseDate(birthDate))
                                        .actorImg(actorDto.getProfilePath())
                                        .build()));
                        return MovieActors.builder().movie(movie).actor(actor).characterName(actorDto.getCharacter()).build();
                    }).collect(Collectors.toList());
            movie.addMovieActors(movieActors);
        }
    }

    private String getImageUrl(String imagePath) {
        return imagePath != null ? "https://image.tmdb.org/t/p/w500/" + imagePath : null;
    }

    private LocalDate parseDate(String date) {
        try {
            return date != null ? LocalDate.parse(date) : null;
        } catch (DateTimeParseException e) {
            log.warn("[날짜 파싱 실패] 입력 값: {}", date);
            return null;
        }
    }

    @Transactional
    public void saveMoviesInTransaction(List<Movie> movies) {
        try {
            movieRepository.saveAll(movies);
            log.info("[저장 완료] 저장된 영화 수: {}", movies.size());
        } catch (Exception e) {
            log.error("[저장 실패] 오류: {}", e.getMessage());
        }
    }
}