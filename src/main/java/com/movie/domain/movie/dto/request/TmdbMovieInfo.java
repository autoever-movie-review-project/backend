package com.movie.domain.movie.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class TmdbMovieInfo {
    private Long id;                       // 영화 ID
    private String title;                  // 번역된 제목
    private String tagline;                // 영화 명언(대표 문구)
    private String overview;               // 줄거리
    private double popularity;             // 인기 수치
    private String backdropPath;           // 배경 이미지 URL
    private String posterPath;             // 포스터 이미지 URL
    private String releaseDate;            // 개봉일
    private double voteAverage;            // 평점
    private int voteCount;                 // 평점을 매긴 사람 수
    private String originalLanguage;       // 원본 언어
    private List<GenreDto> genres;         // 장르 ID 및 이름 리스트
    private Credits credits;               // 출연진 및 감독 정보

    @Data
    public static class Credits {
        private List<DirectorDto> crew;    // 감독 정보 리스트
        private List<ActorDto> cast;       // 배우 정보 리스트
    }

    @Data
    public static class GenreDto {
        private Integer tmdbGenreId;       // 장르 ID
        private String name;               // 장르 이름
    }

    @Data
    public static class DirectorDto {
        private Long tmdbDirectorId;       // 감독 ID
        private String name;               // 감독 이름
        private String birthDate;          // 생년월일
    }

    @Data
    public static class ActorDto {
        private Long tmdbActorId;          // 배우 ID
        private String name;               // 배우 이름
        private String birthDate;          // 생년월일
        private String profilePath;        // 프로필 사진 URL
        private String character;          // 극중 역할 이름
    }
}
