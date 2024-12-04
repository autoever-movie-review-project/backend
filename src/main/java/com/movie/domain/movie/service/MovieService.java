package com.movie.domain.movie.service;

import com.movie.domain.movie.dto.response.MovieDetailResDto;
import com.movie.domain.movie.dto.response.MovieListResDto;

import java.util.List;

public interface MovieService {

    MovieDetailResDto findMovie(Long movieId);

    List<MovieListResDto> findMoviesByMainGenre(String genre, int page);

    List<MovieListResDto> findUpcomingMovies(int page);

    List<MovieListResDto> findTopRatedMovies(int page);

    List<MovieListResDto> findAllMovies(int page);

    List<MovieListResDto> searchMovies(String keyword, int page);

}
