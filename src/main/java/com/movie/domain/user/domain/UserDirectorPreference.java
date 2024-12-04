package com.movie.domain.user.domain;

import com.movie.domain.movie.domain.Director;
import com.movie.global.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
@Table(name = "user_director_preference", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "director_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserDirectorPreference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userDirectorPreferenceId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "director_id", nullable = false)
    private Director director;

    private double preferenceScore;

    @Builder
    public UserDirectorPreference(User user, Director director, double preferenceScore) {
        this.user = user;
        this.director = director;
        this.preferenceScore = preferenceScore;
    }
    public void updateScore(double preferenceScore) {
        this.preferenceScore += preferenceScore;
    }
}
