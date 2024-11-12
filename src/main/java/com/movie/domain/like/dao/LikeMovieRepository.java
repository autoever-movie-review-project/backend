package com.movie.domain.like.dao;

import com.movie.domain.like.domain.LikeMovie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeMovieRepository extends JpaRepository<LikeMovie, Long> {
    boolean existsByUserIdAndMovieId(Long userId, Long movieId);

    Long countByMovieId(Long movieId);
}
