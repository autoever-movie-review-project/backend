package com.movie.domain.movie.service;

public interface TmdbService {

    void initializeMovies();

    void searchAndSaveMovie(Long tmdbId);

    void updateNewMovies();
}
