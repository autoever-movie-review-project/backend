package com.movie.domain.recommendation.dao;

import com.movie.domain.recommendation.domain.Recommendation;
import com.movie.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    void deleteByUser_UserId(Long userId);

    List<Recommendation> findByUser_UserId(Long userId);

    // 특정 사용자의 추천 목록을 점수 순으로 정렬해서 조회
    List<Recommendation> findByUserOrderByScoreDesc(User user);

    void deleteByUser(User user);
}
