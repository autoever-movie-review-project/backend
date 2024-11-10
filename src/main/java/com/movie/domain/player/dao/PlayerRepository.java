package com.movie.domain.player.dao;

import com.movie.domain.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Player findByUserId(Long userId);

    boolean existByGameId(Long gameId);
}
