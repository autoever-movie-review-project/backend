package com.movie.domain.pointHistory.dao;

import com.movie.domain.pointHistory.domain.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    List<PointHistory> findAllByUser_UserId(Long userId);
}
