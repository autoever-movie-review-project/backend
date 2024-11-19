package com.movie.domain.user.domain;

import com.movie.domain.movie.domain.Genre;
import com.movie.global.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
@Table(name = "user_genre_preference", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "genre_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserGenrePreference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userGenrePreferenceId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;

    private double preferenceScore;

    @Builder
    public UserGenrePreference(User user, Genre genre, double preferenceScore) {
        this.user = user;
        this.genre = genre;
        this.preferenceScore = preferenceScore;
    }
    public void updateScore(double preferenceScore) {
        this.preferenceScore += preferenceScore;
    }
}
