package com.movie.domain.user.domain;

import com.movie.domain.movie.domain.Actor;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserActorPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userActorPreferenceId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "actor_id", nullable = false)
    private Actor actor;

    private int preferenceScore;

    @Builder
    public UserActorPreference(User user, Actor actor, int preferenceScore) {
        this.user = user;
        this.actor = actor;
        this.preferenceScore = preferenceScore;
    }
}
