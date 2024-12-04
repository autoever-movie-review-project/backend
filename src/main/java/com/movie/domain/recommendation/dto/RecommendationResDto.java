package com.movie.domain.recommendation.dto;

import com.movie.domain.movie.domain.Movie;
import com.movie.domain.movie.dto.response.MovieListResDto;
import com.movie.domain.user.domain.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class RecommendationResDto {
    //영화 정보
    private Long movieId;
    private Long userId;
    private String mainImg;
    private String backdropImg;
    private Double rating;
    private String title;
    private List<String> genre;
    private List<String> mainGenre;
    private String ageRating;
    private String tagline;

    public static RecommendationResDto entityToResDto(Movie movie, User user) {
        return RecommendationResDto.builder()
                .movieId(movie.getMovieId())
                .userId(user.getUserId())
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
}
