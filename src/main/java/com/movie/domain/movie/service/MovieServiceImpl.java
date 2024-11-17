package com.movie.domain.movie.service;

import com.movie.domain.movie.dao.MovieRepository;
import com.movie.domain.movie.dto.response.MovieDetailResDto;
import com.movie.domain.movie.dto.response.MovieListResDto;
import com.movie.domain.movie.exception.MovieNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.movie.domain.movie.constant.MovieExceptionMessage.MOVIE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    private static final int PAGE_SIZE = 10;

    @Override
    @Transactional(readOnly = true)
    public MovieDetailResDto findMovie(Long movieId) {
        return movieRepository.findById(movieId)
                .map(MovieDetailResDto::entityToResDto)
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieListResDto> getMoviesByMainGenre(String genre, int page) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);
        return movieRepository.findByFilteredGenre(genre, pageRequest).stream()
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieListResDto> getUpcomingMovies(int page) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);
        return movieRepository.findByReleaseDateAfterOrderByReleaseDateAsc(LocalDate.now(), pageRequest).stream()
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieListResDto> getTopRatedMovies(int page) {

        if (page < 1) {
            page = 1;
        }

        // 로그: 페이지 값 확인
        log.info("Received page request: {}", page);

        // page 값이 1보다 작은 경우 오류 로그 추가
        if (page < 1) {
            log.warn("Page number is less than 1: {}", page);
        }

        // page - 1로 조정하여 PageRequest 생성
        PageRequest pageRequest = PageRequest.of(page-1, PAGE_SIZE);

        // 로그: PageRequest 확인
        log.info("Created PageRequest: page = {}, size = {}", pageRequest.getPageNumber(), pageRequest.getPageSize());

        // 조건에 맞는 영화 목록을 찾고 DTO로 변환
        List<MovieListResDto> movies = movieRepository.findByPopularityGreaterThanEqualAndRuntimeGreaterThanEqualAndVoteCountGreaterThanEqualAndRatingGreaterThanEqual(
                        80, 60, 10_000, 7.5, pageRequest).stream()
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());

        // 로그: 반환되는 영화 개수 확인
        log.info("Returning {} movies for page {}", movies.size(), page);

        return movies;
    }


    @Override
    @Transactional(readOnly = true)
    public List<MovieListResDto> getAllMovies(int page) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);
        return movieRepository.findAll(pageRequest).stream()
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieListResDto> searchMovies(String title, String genre, int page) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);
        return movieRepository.findByTitleOrGenreOrMainGenre(title, genre, pageRequest).stream()
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());
    }
}
