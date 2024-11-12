package com.movie.domain.player.dao;

import com.movie.domain.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Player findByUser_UserId(Long userId);

    boolean existsByGameId(Long gameId);

    List<Player> findAllByGameId(Long gameId);
}
