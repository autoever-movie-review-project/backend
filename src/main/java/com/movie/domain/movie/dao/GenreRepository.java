package com.movie.domain.movie.dao;

import com.movie.domain.movie.domain.Director;
import com.movie.domain.movie.domain.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Integer> {
    Optional<Genre> findByTmdbGenreId(Integer tmdbGenreId);
}
