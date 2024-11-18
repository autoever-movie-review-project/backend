package com.movie.domain.movie.dto.response;

import com.movie.domain.movie.domain.Movie;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class MovieDetailResDto {
    private Long movieId;
    private String mainImg;
    private String backdropImg;
    private String title;
    private List<String> genre;
    private List<String> directorName;
    private List<String> actorName;
    private List<String> actorImg;
    private String releaseDate;
    private Double rating;
    private String runtime;
    private String language;
    private Integer reviewCount;
    private String plot;
    private String tagline;
    private String ageRating;

    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    public static MovieDetailResDto entityToResDto(Movie movie) {
        return MovieDetailResDto.builder()
                .movieId(movie.getMovieId())
                .mainImg(movie.getMainImg())
                .backdropImg(movie.getBackdropImg())
                .title(movie.getTitle())
                .genre(movie.getMovieGenres().stream()
                        .map(movieGenre -> movieGenre.getGenre().getGenre())
                        .collect(Collectors.toList()))
                .directorName(movie.getMovieDirectors().stream()
                        .map(movieDirector -> movieDirector.getDirector().getDirectorName())
                        .collect(Collectors.toList()))
                .actorName(movie.getMovieActors().stream()
                        .map(movieActor -> movieActor.getActor().getActorName())
                        .collect(Collectors.toList()))
                .actorImg(movie.getMovieActors().stream()
                        .map(movieActor -> IMAGE_BASE_URL + movieActor.getActor().getActorImg()) // Adding base URL to actor images
                        .collect(Collectors.toList()))
                .releaseDate(movie.getReleaseDate() != null ? movie.getReleaseDate().toString() : null)
                .rating(movie.getRating())
                .runtime(movie.getRuntime() != null ? movie.getRuntime().toString() : null)
                .language(movie.getLanguage())
                .reviewCount(movie.getVoteCount())
                .plot(movie.getPlot())
                .tagline(movie.getTagline())
                .ageRating(movie.getAgeRating())
                .build();
    }
}
