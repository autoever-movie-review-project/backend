package com.movie.domain.movie.service;

import com.movie.domain.movie.dto.response.MovieDetailResDto;
import com.movie.domain.movie.dto.response.MovieListResDto;

import java.util.List;

public interface MovieService {

    MovieDetailResDto findMovie(Long movieId);

    List<MovieListResDto> getMoviesByMainGenre(String genre, int page);

    List<MovieListResDto> getUpcomingMovies(int page);

    List<MovieListResDto> getTopRatedMovies(int page);

    List<MovieListResDto> getAllMovies(int page);

    List<MovieListResDto> searchMovies(String keyword, int page);

}
