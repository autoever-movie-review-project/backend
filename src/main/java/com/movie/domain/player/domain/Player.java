package com.movie.domain.player.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.movie.domain.game.domain.Game;
import com.movie.domain.user.domain.User;
import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Long score;
    private boolean isReady=false;

    public void setIsReady(boolean setIsReady) {
        this.isReady=setIsReady;
    }
}
