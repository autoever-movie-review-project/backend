package com.movie.domain.user.domain;

import com.movie.domain.movie.domain.Director;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserDirectorPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userDirectorPreferenceId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "director_id", nullable = false)
    private Director director;

    private int preferenceScore;

    @Builder
    public UserDirectorPreference(User user, Director director, int preferenceScore) {
        this.user = user;
        this.director = director;
        this.preferenceScore = preferenceScore;
    }
}
