package com.movie.domain.recommendation.domain;

import com.movie.domain.movie.domain.Movie;
import com.movie.domain.user.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    private Double score; // 추천 점수

    @Builder
    public Recommendation(User user, Movie movie, Double score) {
        this.user = user;
        this.movie = movie;
        this.score = score;
    }
}
