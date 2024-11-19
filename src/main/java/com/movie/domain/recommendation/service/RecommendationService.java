package com.movie.domain.recommendation.service;

import com.movie.domain.movie.domain.PopularMovieInfo;
import com.movie.domain.recommendation.dto.RecommendationResDto;
import com.movie.domain.user.domain.User;

import java.util.List;

public interface RecommendationService {

    void generateRecommendationsForAllUsers();

    List<PopularMovieInfo.MovieDetail> getDailyPopularMovies();

    void updateLoginUser();

    void initRecommendations(List<Long> movieIds, double rating);

    void addRecommendations(User user);

    void updatePreferences(Long movieId, double rating);

    List<RecommendationResDto> findRecommendations();
}
