package com.movie.domain.movie.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@RedisHash(value = "boxOfficeMovieInfo") // Redis 저장 키: boxOfficeMovieInfo:{targetDate}
public class BoxOfficeMovieInfo {

    @Id
    private String targetDate;           // Redis 키: 날짜 기준 저장
    private List<MovieDetail> movies;   // 박스오피스 영화 상세 리스트

    @TimeToLive(unit = TimeUnit.DAYS)
    private long expiration;            // TTL: 1일

    @Getter
    @Builder
    public static class MovieDetail {
        private Long movieId;           // 영화 ID
        private String rank;            // 순위
        private String audience;        // 관객 수
        private String mainImg;         // 영화 포스터 URL
        private Double rating;          // 평점
        private String title;           // 영화 제목
        private List<String> genres;    // 장르
        private String ageRating;       // 관람 등급
        private String tagline;         // 태그라인

        public void updateRank(String rank) {
            this.rank = rank;
        }
    }
}
