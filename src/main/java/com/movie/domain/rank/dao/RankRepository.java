package com.movie.domain.rank.dao;

import com.movie.domain.rank.domain.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RankRepository extends JpaRepository<Rank, Long> {
    @Query("SELECT r FROM Rank r WHERE :points BETWEEN r.startPoint AND r.endPoint")
    Optional<Rank> findByPointsBetweenStartAndEnd(@Param("points") Integer points);
}
