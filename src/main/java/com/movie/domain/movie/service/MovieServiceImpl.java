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
import java.util.Collections;
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

        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);

        List<MovieListResDto> movies = movieRepository.findTopRatedMovies(pageRequest).stream()
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());

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
    public List<MovieListResDto> searchMovies(String keyword, int page) {
        // 키워드가 null이거나 빈 문자열일 경우 빈 결과 반환
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }

        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);

        return movieRepository.searchMoviesByKeyword(keyword, pageRequest)
                .stream()
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());
    }
}
