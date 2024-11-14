package com.movie.domain.movie.service;

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
    private final RestTemplate restTemplate;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;

    // TMDB에서 최신 영화 ID 기준 -5000부터 데이터 가져와 DB에 추가하는 메서드
    public void initializeMovies() {
        int moviesFetched = 0;
        int page = 1;
        int targetCount = 50; // 원하는 영화 개수

        while (moviesFetched < targetCount) {
            String url = String.format("/movie/popular?page=%d&language=ko-KR&append_to_response=credits", page);
            HttpEntity<String> entity = new HttpEntity<>(new HttpHeaders());

            try {
                // TmdbMovieResDto를 사용하여 인기 영화 목록을 받아옴
                ResponseEntity<TmdbMovieResDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, TmdbMovieResDto.class);
                List<TmdbMovieInfo> popularMovies = response.getBody().getResults();

                if (popularMovies == null || popularMovies.isEmpty()) {
                    log.info("더 이상 인기 영화 데이터가 없습니다.");
                    break;
                }

                // 각 TmdbMovieInfo 객체를 addMovieToDb 메서드에 전달하여 DB에 추가
                for (TmdbMovieInfo movieInfo : popularMovies) {
                    if (!movieRepository.existsByTmdbId(movieInfo.getId())) {
                        addMovieToDb(movieInfo); // TmdbMovieInfo 객체를 직접 전달
                        moviesFetched++;
                        if (moviesFetched >= targetCount) {
                            break;
                        }
                    }
                }

                page++; // 다음 페이지로 이동
            } catch (HttpClientErrorException e) {
                log.error("인기 TMDB 영화 조회 오류: 상태 코드 = {}, 응답 내용 = {}", e.getStatusCode(), e.getResponseBodyAsString());
                break;
            }
        }

        log.info("인기 영화 데이터 수집 완료 - 총 추가된 영화 개수: {}", moviesFetched);
    }


    @Override
    public TmdbMovieInfo getMoviesWithCredits(Long movieId) {
        String url = String.format("/movie/%d?append_to_response=credits&language=ko-KR", movieId);
        log.info("Requesting movie details from TMDB API: URL = {}, movieId = {}", url, movieId);

        HttpEntity<String> entity = new HttpEntity<>(new HttpHeaders());

        try {
            ResponseEntity<TmdbMovieInfo> response = restTemplate.exchange(url, HttpMethod.GET, entity, TmdbMovieInfo.class);
            log.info("Response received from TMDB API: Status Code = {}", response.getStatusCode());
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("TMDB 요청 오류: 상태 코드 = {}, 응답 내용 = {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        }
    }

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

    @Transactional
    @Override
    public void addMovieToDb(TmdbMovieInfo movieInfo) {
        if (movieInfo == null || movieRepository.existsByTmdbId(movieInfo.getId())) {
            log.info("[중복 확인]: 영화 이미 존재하거나 TMDB 정보가 없음 - TMDB ID = {}", movieInfo.getId());
            return;
        }

        log.info("[Movie 엔티티 생성 시작]: TMDB ID = {}", movieInfo.getId());
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
                .build();

        // MovieGenres 추가 - null 체크
        if (movieInfo.getGenres() != null && !movieInfo.getGenres().isEmpty()) {
            List<MovieGenres> movieGenres = movieInfo.getGenres().stream()
                    .filter(genreDto -> genreDto != null) // 리스트 내부 객체 null 체크
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

        // MovieDirectors 추가 - null 체크
        if (movieInfo.getCredits() != null && movieInfo.getCredits().getCrew() != null && !movieInfo.getCredits().getCrew().isEmpty()) {
            List<MovieDirectors> movieDirectors = movieInfo.getCredits().getCrew().stream()
                    .filter(directorDto -> directorDto != null) // 리스트 내부 객체 null 체크
                    .map(directorDto -> {
                        var director = directorRepository.findByTmdbDirectorId(directorDto.getTmdbDirectorId())
                                .orElseGet(() -> directorRepository.save(
                                        Director.builder()
                                                .tmdbDirectorId(directorDto.getTmdbDirectorId())
                                                .directorName(directorDto.getName())
                                                .birthDate(parseBirthDate(directorDto.getBirthDate()))
                                                .build()
                                ));
                        return MovieDirectors.builder().movie(movie).director(director).build();
                    })
                    .collect(Collectors.toList());
            movie.addMovieDirectors(movieDirectors);
        }

        // MovieActors 추가 - null 체크
        if (movieInfo.getCredits() != null && movieInfo.getCredits().getCast() != null && !movieInfo.getCredits().getCast().isEmpty()) {
            List<MovieActors> movieActors = movieInfo.getCredits().getCast().stream()
                    .filter(actorDto -> actorDto != null) // 리스트 내부 객체 null 체크
                    .limit(10)  // 최대 10명의 배우만 추출
                    .map(actorDto -> {
                        var actor = actorRepository.findByTmdbActorId(actorDto.getTmdbActorId())
                                .orElseGet(() -> actorRepository.save(
                                        Actor.builder()
                                                .tmdbActorId(actorDto.getTmdbActorId())
                                                .actorName(actorDto.getName())
                                                .birthDate(parseBirthDate(actorDto.getBirthDate()))
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

