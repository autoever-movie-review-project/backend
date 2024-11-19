package com.movie.domain.user.domain;

import com.movie.domain.movie.domain.Actor;
import com.movie.global.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table(name = "user_actor_preference", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "actor_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserActorPreference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userActorPreferenceId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "actor_id", nullable = false)
    private Actor actor;

    private double preferenceScore;

    @Builder
    public UserActorPreference(User user, Actor actor, double preferenceScore) {
        this.user = user;
        this.actor = actor;
        this.preferenceScore = preferenceScore;
    }

    public void updateScore(double preferenceScore) {
        this.preferenceScore += preferenceScore;
    }

}
