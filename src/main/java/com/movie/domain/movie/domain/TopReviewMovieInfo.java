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
@RedisHash(value = "topReviewMovieInfo") // Redis 저장 키: topReviewMovieInfo:{targetDate}
public class TopReviewMovieInfo {

    @Id
    private String targetDate;
    private List<MovieDetail> reviews;

    @TimeToLive(unit = TimeUnit.DAYS)
    private long expiration;            // TTL: 1일

    @Getter
    @Builder
    public static class MovieDetail {
        private Long movieId;
        private String rank;            // 순위
        private Long reviewCount;       // 일주일 간 리뷰 수
        private String mainImg;
        private Double rating;
        private String title;
        private List<String> genres;
        private String ageRating;
        private String tagline;

        public void updateRank(String rank) {
            this.rank = rank;
        }
    }
}
