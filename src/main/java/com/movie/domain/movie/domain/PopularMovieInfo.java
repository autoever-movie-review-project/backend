package com.movie.domain.movie.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@RedisHash(value = "popularMovieInfo") // Redis 저장 키: popularMovieInfo:{targetDate}
public class PopularMovieInfo  {
    @Id
    private String targetDate;
    private List<MovieDetail> movies;

    @TimeToLive(unit = TimeUnit.DAYS)
    private long expiration;

    @Getter
    @Builder
    public static class MovieDetail {
        private Long movieId;
        private float popularityScore;
        private List<Integer> genreIds;
        private List<Long> actorIds;
        private List<Long> directorIds;
    }
}
