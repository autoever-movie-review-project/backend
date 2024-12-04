package com.movie.domain.movie.dao;

import com.movie.domain.movie.domain.Director;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DirectorRepository extends JpaRepository<Director, Long> {
    Optional<Director> findByTmdbDirectorId(Long tmdbDirectorId);
}
