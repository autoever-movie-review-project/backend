package com.movie.domain.movie.dto.response;

import com.movie.domain.movie.domain.Movie;
import com.movie.domain.movie.domain.TopReviewMovieInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class TopReviewedMoviesResDto {
    private Long movieId;
    private String rank;
    private Long reviewCount;
    private String mainImg;
    private String backdropImg;
    private Double rating;
    private String title;
    private List<String> genre;
    private String ageRating;
    private String tagline;

    public static TopReviewedMoviesResDto entityToResDto(Movie movie, Long reviewCount, String rank) {
        return TopReviewedMoviesResDto.builder()
                .movieId(movie.getMovieId())
                .rank(rank)
                .reviewCount(reviewCount)
                .mainImg(movie.getMainImg())
                .backdropImg(movie.getBackdropImg())
                .rating(movie.getRating())
                .title(movie.getTitle())
                .genre(movie.getMovieGenres().stream()
                        .map(movieGenre -> movieGenre.getGenre().getGenre())
                        .collect(Collectors.toList()))
                .ageRating(movie.getAgeRating())
                .tagline(movie.getTagline())
                .build();
    }

    public static TopReviewedMoviesResDto entityToResDto(TopReviewMovieInfo.MovieDetail detail) {
        return TopReviewedMoviesResDto.builder()
                .movieId(detail.getMovieId())
                .rank(detail.getRank())
                .reviewCount(detail.getReviewCount())
                .mainImg(detail.getMainImg())
                .backdropImg(detail.getBackdropImg())
                .rating(detail.getRating())
                .title(detail.getTitle())
                .genre(detail.getGenres())
                .ageRating(detail.getAgeRating())
                .tagline(detail.getTagline())
                .build();
    }
}
