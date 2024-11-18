package com.movie.domain.recommendation.domain;

import com.movie.domain.movie.domain.Movie;
import com.movie.domain.user.domain.User;

import javax.persistence.*;
import java.util.Date;

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

    private Float score; // 추천 점수
}
