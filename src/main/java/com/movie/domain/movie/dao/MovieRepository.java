package com.movie.domain.movie.dao;

import com.movie.domain.movie.domain.Movie;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    // TMDB ID가 존재하는지 확인
    boolean existsByTmdbId(Long tmdbId);

    // 특정 장르의 영화 가져오기
    List<Movie> findByMovieGenres_Genre_Genre(String genre);

    // 특정 날짜 이후 개봉하는 영화 가져오기
    List<Movie> findByReleaseDateAfter(LocalDate date);

    // 특정 날짜 이전 또는 같은 날짜에 개봉한 영화 가져오기
    List<Movie> findByReleaseDateLessThanEqual(LocalDate date);

    // 평점 높은 순으로 영화 가져오기
    List<Movie> findByOrderByRatingDesc();

    // 특정 제목과 특정 장르가 일치하는 영화 가져오기
    List<Movie> findByTitleContainingAndMovieGenres_Genre_Genre(String title, String genre);

    // 개봉 예정 영화 (오늘 이후 개봉일 기준, 상위 N개)
    List<Movie> findByReleaseDateAfterOrderByReleaseDateAsc(LocalDate date, Pageable pageable);

    // 현재 상영 중인 영화 (오늘 이전에 개봉한 영화, 평점 순)
    @Query("SELECT m FROM Movie m WHERE m.releaseDate <= :today ORDER BY m.releaseDate DESC")
    List<Movie> findNowShowingMovies(@Param("today") LocalDate today, Pageable pageable);

    // 여러 TMDB ID가 존재하는지 확인
    @Query("SELECT m.tmdbId FROM Movie m WHERE m.tmdbId IN :tmdbIds")
    List<Long> findTmdbIds(@Param("tmdbIds") List<Long> tmdbIds);

    // 최대 TMDB ID 가져오기
    @Query("SELECT MAX(m.tmdbId) FROM Movie m")
    Optional<Long> findMaxTmdbId();
}
