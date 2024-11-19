package com.movie.domain.movie.dao;

import com.movie.domain.movie.domain.Actor;
import com.movie.domain.movie.domain.Movie;
import com.movie.domain.user.domain.User;
import com.movie.domain.user.domain.UserActorPreference;
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

    @Query("SELECT m FROM Movie m " +
            "LEFT JOIN FETCH m.movieGenres " +
            "WHERE LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(m.title, ' ', ''), ':', ''), 'Ⅱ', '2'), 'Ⅲ', '3'), 'Ⅳ', '4')) " +
            "LIKE LOWER(CONCAT('%', REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(:title, ' ', ''), ':', ''), 'Ⅱ', '2'), 'Ⅲ', '3'), 'Ⅳ', '4'), '%')) " +
            "AND (:startDate IS NULL OR :endDate IS NULL OR m.releaseDate BETWEEN :startDate AND :endDate)")
    Optional<Movie> findByTitleAndReleaseDateRange(
            @Param("title") String title,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


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
            "WHERE m.popularity >= 80 " +
            "AND m.runtime >= 60 " +
            "AND m.voteCount >= 10000 " +
            "AND m.rating >= 7.5")
    Page<Movie> findTopRatedMovies(Pageable pageable);


    // mainGenre 또는 genre로 영화 조회
    @Query("SELECT m FROM Movie m " +
            "JOIN m.movieGenres mg " +
            "JOIN mg.genre g " +
            "WHERE (g.mainGenre = :genre OR g.genre = :genre) " +
            "AND m.rating >= 4 " +
            "AND m.runtime >= 60 " +
            "AND m.voteCount >= 500")
    Page<Movie> findByFilteredGenre(@Param("genre") String genre, Pageable pageable);

    //검색
    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN m.movieGenres mg " +
            "LEFT JOIN mg.genre g " +
            "WHERE m.title LIKE %:keyword% " +
            "OR g.genre LIKE %:keyword% " +
            "OR g.mainGenre LIKE %:keyword%")
    Page<Movie> searchMoviesByKeyword(@Param("keyword") String keyword, Pageable pageable);

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

    Optional<Movie> findByMovieId(Long movieId);

    // 인기순 상위 50개 영화 가져오기
    List<Movie> findTop50ByOrderByPopularityDesc();

    @Query("SELECT m FROM Movie m WHERE (m.language IN :languages) AND (m.voteCount >= :minVotes) ORDER BY m.popularity DESC")
    List<Movie> findTop50ByPopularityAndLanguage(@Param("languages") List<String> languages, @Param("minVotes") int minVotes);

}
