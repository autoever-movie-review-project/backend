package com.movie.domain.movie.service;

import com.movie.domain.movie.dto.response.MovieDetailResDto;
import com.movie.domain.movie.dto.response.MovieListResDto;

import java.util.List;

public interface MovieService {

    MovieDetailResDto findMovie(Long movieId);

    List<MovieListResDto> getMoviesByMainGenre(String genre);

    List<MovieListResDto> getUpcomingMovies();

    List<MovieListResDto> getTopRatedMovies();

    List<MovieListResDto> getAllMovies();

    List<MovieListResDto> searchMovies(String title, String genre);
}
