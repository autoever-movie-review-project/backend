package com.movie.domain.recommendation.dao;

import com.movie.domain.movie.domain.Movie;

import java.util.List;

public interface RecommendationRepositoryCustom {
    List<Movie> findRecommendedMoviesByUserPreferences(Long userId);
}
