package com.movie.domain.movie.dao;

import com.movie.domain.movie.domain.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    boolean existsByTmdbId(Long tmdbId);

    @Query("SELECT MAX(m.tmdbId) FROM Movie m")
    Optional<Long> findMaxTmdbId();
}
