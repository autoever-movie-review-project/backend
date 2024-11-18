package com.movie.domain.game.dao;

import com.movie.domain.game.domain.Game;
import com.movie.domain.game.domain.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {

    // status가 WAITING이고, 현재 playerCount가 maxPlayer 미만인 게임 조회
    @Query("SELECT g FROM Game g WHERE g.status = :status AND (g.maxPlayer - g.playerCount) > 0")
    List<Game> findAvailableGames(@Param("status") GameStatus status);

    boolean existsByHostId(Long hostId);
}
