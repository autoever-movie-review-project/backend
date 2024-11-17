package com.movie.domain.movie.dao;

import com.movie.domain.movie.domain.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findByTitleAndReleaseDate(String title, LocalDate releaseDate);

    boolean existsByTitleAndReleaseDate(String title, LocalDate releaseDate);

    // TMDB ID가 존재하는지 확인
    @Query("SELECT m.tmdbId FROM Movie m WHERE m.tmdbId IN :tmdbIds")
    List<Long> findTmdbIds(@Param("tmdbIds") List<Long> tmdbIds);

    // mainGenre로 영화 조회
    Page<Movie> findByMovieGenres_Genre_MainGenre(String mainGenre, Pageable pageable);

    // 개봉일 기준으로 영화 조회
    Page<Movie> findByReleaseDateAfterOrderByReleaseDateAsc(LocalDate date, Pageable pageable);

    // 평점 및 조건을 기반으로 영화 조회
    @Query("SELECT m FROM Movie m " +
            "WHERE m.popularity >= :popularity AND m.runtime >= :runtime AND m.voteCount >= :voteCount AND m.rating >= :rating")
    Page<Movie> findByPopularityGreaterThanEqualAndRuntimeGreaterThanEqualAndVoteCountGreaterThanEqualAndRatingGreaterThanEqual(
            int popularity, int runtime, int voteCount, double rating, Pageable pageable);

    // mainGenre 또는 genre로 영화 조회
    @Query("SELECT m FROM Movie m " +
            "JOIN m.movieGenres mg " +
            "JOIN mg.genre g " +
            "WHERE (g.mainGenre = :genre OR g.genre = :genre) " +
            "AND m.rating >= 4 " +
            "AND m.runtime >= 60 " +
            "AND m.voteCount >= 500")
    Page<Movie> findByFilteredGenre(@Param("genre") String genre, Pageable pageable);

    // 제목 또는 장르로 영화 검색 (페이지네이션 추가)
    @Query("SELECT m FROM Movie m " +
            "JOIN m.movieGenres mg " +
            "JOIN mg.genre g " +
            "WHERE m.title LIKE %:title% " +
            "OR (g.genre = :genre OR g.mainGenre = :genre)")
    Page<Movie> findByTitleOrGenreOrMainGenre(@Param("title") String title, @Param("genre") String genre, Pageable pageable);

    // 리뷰 수를 기반으로 영화 검색
    @Query(value = "SELECT m.movie_id AS movieId, COUNT(r.review_id) AS reviewCount " +
            "FROM review r " +
            "JOIN movie m ON r.movie_id = m.movie_id " +
            "WHERE r.created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY m.movie_id " +
            "ORDER BY reviewCount DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findTopMoviesByReviewCount(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate,
                                              @Param("limit") int limit);
}
