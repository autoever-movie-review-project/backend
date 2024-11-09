package com.movie.domain.gameroom.dao;

import com.movie.domain.gameroom.domain.GameRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {

}
