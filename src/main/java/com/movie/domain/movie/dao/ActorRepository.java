package com.movie.domain.movie.dao;

import com.movie.domain.movie.domain.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActorRepository extends JpaRepository<Actor, Long> {
    Optional<Actor> findByTmdbActorId(Long tmdbActorId);
}
