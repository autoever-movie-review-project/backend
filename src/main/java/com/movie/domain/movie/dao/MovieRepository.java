package com.movie.domain.movie.dao;

import com.movie.domain.movie.domain.Movie;
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

    List<Movie> findByMovieGenres_Genre_MainGenre(String MainGenre);

    List<Movie> findByReleaseDateAfterOrderByReleaseDateAsc(LocalDate date, Pageable pageable);

    List<Movie> findByPopularityGreaterThanEqualAndRuntimeGreaterThanEqualAndVoteCountGreaterThanEqualAndRatingGreaterThanEqual(
            int popularity, int runtime, int voteCount, double rating);

    @Query("SELECT m FROM Movie m " +
            "JOIN m.movieGenres mg " +
            "JOIN mg.genre g " +
            "WHERE (g.mainGenre = :genre OR g.genre = :genre) " +
            "AND m.rating >= 4 " +
            "AND m.runtime >= 60 " +
            "AND m.voteCount >= 500")
    List<Movie> findByFilteredGenre(@Param("genre") String genre);

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

    @Query("SELECT m FROM Movie m " +
            "JOIN m.movieGenres mg " +
            "JOIN mg.genre g " +
            "WHERE m.title LIKE %:title% " +
            "OR (g.genre = :genre OR g.mainGenre = :genre)")
    List<Movie> findByTitleOrGenreOrMainGenre(@Param("title") String title, @Param("genre") String genre);

}
