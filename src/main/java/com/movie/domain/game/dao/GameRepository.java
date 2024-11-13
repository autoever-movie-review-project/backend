package com.movie.domain.game.dao;

import com.movie.domain.game.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findAllById(Long gameId);
}
