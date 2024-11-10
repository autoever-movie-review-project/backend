package com.movie.domain.game.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table
@Getter
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long hostId;
    private String title;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    private Integer maxPlayer;
    private Integer playerCount;

    public void gameStart() {
        this.status = GameStatus.STARTED;
    }

    public void gameEnd() {
        this.status = GameStatus.FINISHED;
    }

    public void gameWait() {
        this.status = GameStatus.WAITING;
    }

    public void setPlayerCount() {
        this.playerCount = this.playerCount + 1;
    }
}
