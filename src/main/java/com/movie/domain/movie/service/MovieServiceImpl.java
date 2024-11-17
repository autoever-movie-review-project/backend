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

    @Override
    @Transactional(readOnly = true)
    public MovieDetailResDto findMovie(Long movieId) {
        return movieRepository.findById(movieId)
                .map(MovieDetailResDto::entityToResDto)
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieListResDto> getMoviesByMainGenre(String genre) {
        // mainGenre가 포함된 영화들을 조회
        return movieRepository.findByFilteredGenre(genre).stream()
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieListResDto> getUpcomingMovies() {
        return movieRepository.findByReleaseDateAfterOrderByReleaseDateAsc(LocalDate.now(), PageRequest.of(0, 5)).stream()
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieListResDto> getTopRatedMovies() {
        return movieRepository.findByPopularityGreaterThanEqualAndRuntimeGreaterThanEqualAndVoteCountGreaterThanEqualAndRatingGreaterThanEqual(
                        80, 60, 10_000, 7.5).stream()
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieListResDto> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieListResDto> searchMovies(String title, String genre) {
        return movieRepository.findByTitleOrGenreOrMainGenre(title, genre).stream()
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());
    }
}
