package com.movie.domain.movie.service;

import com.movie.domain.movie.dao.MovieRepository;
import com.movie.domain.movie.domain.Movie;
import com.movie.domain.movie.dto.response.MovieDetailResDto;
import com.movie.domain.movie.dto.response.MovieListResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    @Override
    @Transactional(readOnly = true)
    public MovieDetailResDto findMovie(Long movieId) {
        Optional<Movie> movieOptional = movieRepository.findById(movieId);
        return movieOptional.map(MovieDetailResDto::entityToResDto).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieListResDto> getMoviesByGenre(String genre) {
        return movieRepository.findByMovieGenres_Genre_Genre(genre).stream()
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieListResDto> getRealtimeMovies() {
        return movieRepository.findByReleaseDateAfter(LocalDate.now()).stream()
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
    public List<MovieListResDto> getNowShowingMovies() {
        return movieRepository.findNowShowingMovies(LocalDate.now(), PageRequest.of(0, 5)).stream()
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieListResDto> getTopRatedMovies() {
        return movieRepository.findByOrderByRatingDesc().stream()
                .map(MovieListResDto::entityToResDto)
                .limit(5)
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
        return movieRepository.findByTitleContainingAndMovieGenres_Genre_Genre(title, genre).stream()
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());
    }
}
