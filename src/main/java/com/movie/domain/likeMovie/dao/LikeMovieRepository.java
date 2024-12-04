package com.movie.domain.likeMovie.dao;

import com.movie.domain.likeMovie.domain.LikeMovie;
import com.movie.domain.movie.domain.Movie;
import com.movie.domain.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeMovieRepository extends JpaRepository<LikeMovie, Long> {
    boolean existsByUserUserIdAndMovieMovieId(Long userId, Long movieId);

    Long countByMovieMovieId(Long movieId);

    LikeMovie findByUserUserIdAndMovieMovieId(Long userId, Long movieId);

    Page<LikeMovie> findByUserUserId(Long userId, Pageable pageable);

    boolean existsByUser_UserIdAndMovie_MovieId(Long userId, Long movieId);

    boolean existsByUserAndMovie(User user, Movie movie);

}
