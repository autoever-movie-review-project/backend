package com.movie.domain.review.domain;

import com.movie.domain.movie.domain.Movie;
import com.movie.domain.user.domain.User;
import com.movie.global.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(columnDefinition = "TEXT")
    private String content;

    private int likesCount; // 리뷰 좋아요 수

    private double rating; // 평점

    @Builder
    public Review(User user, Movie movie, String content, double rating) {
        this.user = user;
        this.movie = movie;
        this.content = content;
        this.rating = rating;
        this.likesCount = 0;
    }

    public void upLikeCount()
    {
        this.likesCount += 1;
    }}
