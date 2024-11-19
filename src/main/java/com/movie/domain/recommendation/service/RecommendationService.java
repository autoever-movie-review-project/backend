package com.movie.domain.recommendation.service;

import com.movie.domain.movie.domain.Movie;
import com.movie.domain.user.domain.User;

import java.util.List;

public interface RecommendationService {

    void generateRecommendationsAsync(User user);

    List<Movie> getRecommendations(User user);

    void updatePreferences(Long movieId);
}
