package com.movie.domain.movie.dto.response;

import com.movie.domain.movie.domain.Movie;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class BoxOfficeListResDto {
    private Long movieId;
    private String rank;         // 순위
    private String audience;     //관객수
    private String mainImg;
    private Double rating;
    private String title;
    private List<String> genre;
    private String ageRating;
    private String tagline;

    public static BoxOfficeListResDto entityToResDto(Movie movie, String rank, String audience) {
        return BoxOfficeListResDto.builder()
                .movieId(movie.getMovieId())
                .rank(rank)
                .audience(audience)
                .mainImg(movie.getMainImg())
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
