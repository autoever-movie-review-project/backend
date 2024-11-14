package com.movie.domain.movie.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TmdbMovieInfo {

    private Long id;                       // 영화 ID
    private String title;                  // 번역된 제목
    private String tagline;                // 영화 명언(대표 문구)
    private String overview;               // 줄거리
    private double popularity;             // 인기 수치

    @JsonProperty("backdrop_path")
    private String backdropPath;           // 배경 이미지 URL

    @JsonProperty("poster_path")
    private String posterPath;             // 포스터 이미지 URL

    @JsonProperty("release_date")
    private String releaseDate;            // 개봉일

    @JsonProperty("vote_average")
    private double voteAverage;            // 평점

    @JsonProperty("vote_count")
    private int voteCount;                 // 평점을 매긴 사람 수

    @JsonProperty("original_language")
    private String originalLanguage;       // 원본 언어

    @JsonProperty("runtime")
    private Integer runtime;               // 상영 시간

    @JsonProperty("age_rating")
    private String ageRating;              // 연령 등급

    private List<GenreDto> genres;         // 장르 ID 및 이름 리스트
    private Credits credits;               // 출연진 및 감독 정보

    @JsonProperty("release_dates")
    private List<ReleaseDateResult> releaseDates; // 국가별 개봉 날짜 및 등급 정보

    @Data
    public static class Credits {

        private List<DirectorDto> crew;    // 감독 정보 리스트
        private List<ActorDto> cast;       // 배우 정보 리스트
    }

    @Data
    public static class GenreDto {

        @JsonProperty("id")
        private Integer tmdbGenreId;       // 장르 ID

        private String name;               // 장르 이름
    }

    @Data
    public static class DirectorDto {

        @JsonProperty("id")
        private Long tmdbDirectorId;       // 감독 ID

        private String name;               // 감독 이름

        @JsonProperty("birth_date")        // 생년월일
        private String birthDate;

        private String job;                // 직업 (예: "Director")
    }

    @Data
    public static class ActorDto {

        @JsonProperty("id")
        private Long tmdbActorId;          // 배우 ID

        private String name;               // 배우 이름

        @JsonProperty("birth_date")        // 생년월일
        private String birthDate;

        @JsonProperty("profile_path")
        private String profilePath;        // 프로필 사진 URL

        private String character;          // 극중 역할 이름

        private Integer order;             // 출연 순서
    }

    @Data
    public static class ReleaseDateResult {

        @JsonProperty("iso_3166_1")
        private String countryCode;        // 국가 코드 (예: "KR" 등)

        @JsonProperty("release_dates")
        private List<Certification> certifications; // 국가별 certification 정보
    }

    @Data
    public static class Certification {

        private String certification;      // 영화 등급
        private String releaseDate;        // 개봉 날짜
    }
}
