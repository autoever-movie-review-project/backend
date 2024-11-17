package com.movie.domain.movie.service;

import com.movie.domain.movie.dto.response.MovieDetailResDto;
import com.movie.domain.movie.dto.response.MovieListResDto;

import java.util.List;

public interface MovieService {

    MovieDetailResDto findMovie(Long movieId);

    List<MovieListResDto> getMoviesByMainGenre(String genre, int page); // 페이지 번호 추가

    List<MovieListResDto> getUpcomingMovies(int page); // 페이지 번호 추가

    List<MovieListResDto> getTopRatedMovies(int page); // 페이지 번호 추가

    List<MovieListResDto> getAllMovies(int page); // 페이지 번호 추가

    List<MovieListResDto> searchMovies(String title, String genre, int page); // 페이지 번호 추가
}
