package com.movie.domain.PointHistory.dao;

import com.movie.domain.PointHistory.domain.PointHistory;
import com.movie.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    List<PointHistory> findAllByUser_UserId(Long userId);
}
