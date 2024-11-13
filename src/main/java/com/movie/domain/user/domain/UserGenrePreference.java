package com.movie.domain.user.domain;

import com.movie.domain.movie.domain.Genre;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserGenrePreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userGenrePreferenceId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;

    private int preferenceScore;

    @Builder
    public UserGenrePreference(User user, Genre genre, int preferenceScore) {
        this.user = user;
        this.genre = genre;
        this.preferenceScore = preferenceScore;
    }
}
