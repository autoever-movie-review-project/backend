package com.movie.domain.likeMovie.dao;

import com.movie.domain.likeMovie.domain.LikeMovie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeMovieRepository extends JpaRepository<LikeMovie, Long> {
    boolean existsByUserUserIdAndMovieMovieId(Long userId, Long movieId);

    Long countByMovieMovieId(Long movieId);

    LikeMovie findByUserUserIdAndMovieMovieId(Long userId, Long movieId);

    Page<LikeMovie> findByUserUserId(Long userId, Pageable pageable);
}
