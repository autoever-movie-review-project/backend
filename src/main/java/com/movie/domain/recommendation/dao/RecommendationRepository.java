package com.movie.domain.recommendation.dao;

import com.movie.domain.recommendation.domain.Recommendation;
import com.movie.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByUserOrderByScoreDesc(User user);
}
