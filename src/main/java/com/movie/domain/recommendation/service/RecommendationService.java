
package com.movie.domain.recommendation.service;

import com.movie.domain.recommendation.dto.RecommendationResDto;
import com.movie.domain.user.domain.User;

import java.util.List;

public interface RecommendationService {

    void addRecommendations(User user);

    void updatePreferences(Long movieId, double rating);

    void deleteRecommendations(Long userId);

    List<RecommendationResDto> findRecommendations();

    void updateLoginUser();
}
