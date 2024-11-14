package com.movie.domain.movie.service;

import com.movie.domain.movie.dto.request.TmdbMovieInfo;

import java.time.LocalDate;

public interface TmdbService {
    void initializeMovies();
    TmdbMovieInfo getMoviesWithCredits(Long movieId);
    String getImageUrl(String imagePath);
//    void addNewMovies();
    void updateNewMovies();
    void addMovieToDb(TmdbMovieInfo movieInfo);
    LocalDate parseBirthDate(String birthDate);
}
