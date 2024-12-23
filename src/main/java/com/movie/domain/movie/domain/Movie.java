package com.movie.domain.movie.domain;

import com.movie.domain.review.domain.Review;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "tmdb_id", nullable = false, unique = true)
    private Long tmdbId;                   // TMDB 영화 ID

    private String title;                  // 번역된 제목

    private String tagline;                // 영화 대표 문구

    @Column(columnDefinition = "TEXT")
    private String plot;                   // 줄거리

    private double popularity;             // 인기 수치

    @Column(name = "backdrop_img")
    private String backdropImg;            // 배경 이미지 URL

    @Column(name = "main_img")
    private String mainImg;                // 메인 포스터 이미지 URL

    @Column(name = "release_date")
    private LocalDate releaseDate;         // 개봉일

    private double rating;                 // 평점

    @Column(name = "vote_count")
    private int voteCount;                 // 평점을 매긴 사람 수

    private String language;               // 원본 언어

    private Integer runtime;               // 러닝타임

    @Column(name = "age_rating")
    private String ageRating;              // 관람 등급

    @Column(name = "review_count")
    private Integer reviewCount;           // 리뷰 수

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieGenres> movieGenres;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieActors> movieActors;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieDirectors> movieDirectors;

    @Builder
    public Movie(Long tmdbId, String title, String tagline, String plot, double popularity, String backdropImg,
                 String mainImg, LocalDate releaseDate, double rating, int voteCount, String language,
                 Integer runtime, String ageRating, Integer reviewCount, List<Review> reviews,
                 List<MovieGenres> movieGenres, List<MovieActors> movieActors, List<MovieDirectors> movieDirectors) {
        this.tmdbId = tmdbId;
        this.title = title;
        this.tagline = tagline;
        this.plot = plot;
        this.popularity = popularity;
        this.backdropImg = backdropImg;
        this.mainImg = mainImg;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.voteCount = voteCount;
        this.language = language;
        this.runtime = runtime;
        this.ageRating = ageRating;
        this.reviewCount = reviewCount;
        this.reviews = reviews;
        this.movieGenres = movieGenres;
        this.movieActors = movieActors;
        this.movieDirectors = movieDirectors;
    }

    public void addMovieGenres(List<MovieGenres> genres) {
        if (this.movieGenres == null) {
            this.movieGenres = new ArrayList<>();
        }
        if (genres != null) {
            this.movieGenres.addAll(genres);
        }
    }

    public void addMovieDirectors(List<MovieDirectors> directors) {
        if (this.movieDirectors == null) {
            this.movieDirectors = new ArrayList<>();
        }
        if (directors != null) {
            this.movieDirectors.addAll(directors);
        }
    }

    public void addMovieActors(List<MovieActors> actors) {
        if (this.movieActors == null) {
            this.movieActors = new ArrayList<>();
        }
        if (actors != null) {
            this.movieActors.addAll(actors);
        }
    }

    //평점 업데이트 (추가 or 삭제)
    public void updateRating(double rating, boolean isAddition) {
        if (isAddition) {
            // 리뷰 추가 시
            this.rating = (this.rating * this.voteCount + rating) / (this.voteCount + 1);
            this.voteCount++;

            if (this.reviewCount == null) {
                this.reviewCount = 1;
            } else {
                this.reviewCount++;
            }
        } else {
            // 리뷰 삭제 시
            if (this.voteCount <= 1) {
                // 리뷰가 마지막 1개인 경우
                this.rating = 0.0;
                this.voteCount = 0;
            } else {
                this.rating = (this.rating * this.voteCount - rating) / (this.voteCount - 1);
                this.voteCount--;
            }

            if (this.reviewCount != null && this.reviewCount > 0) {
                this.reviewCount--;
            }
        }
    }

}
