package com.movie.domain.recommendation.service;

import com.movie.domain.user.domain.User;
import com.movie.domain.movie.domain.Movie;
import java.util.List;

public interface RecommendationService {
    void generateRecommendationsAsync(User user);
    List<Movie> getRecommendations(User user);
    void updatePreferencesAfterAction(User user, Movie movie, int weight);
}
