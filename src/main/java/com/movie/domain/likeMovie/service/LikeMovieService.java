package com.movie.domain.likeMovie.service;

import com.movie.domain.likeMovie.dao.LikeMovieRepository;
import com.movie.domain.likeMovie.domain.LikeMovie;
import com.movie.domain.likeMovie.exception.LikeMovieDuplicateException;
import com.movie.domain.movie.dao.MovieRepository;
import com.movie.domain.movie.domain.Movie;
import com.movie.domain.user.dao.UserRepository;
import com.movie.domain.user.domain.User;
import com.movie.global.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeMovieService {
    private final LikeMovieRepository likeMovieRepository;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    @Transactional
    public Long save(Long movieId) {
        // 로그인 된 유저를 가져온다.
        User loggedInUser = securityUtils.getLoginUser();

        if (likeMovieRepository.existsByUserUserIdAndMovieId(loggedInUser.getUserId(), movieId)) {
            throw new LikeMovieDuplicateException("좋아요 중복");
        }

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow();

        LikeMovie likeMovie = LikeMovie.builder()
                .movie(movie)
                .user(loggedInUser)
                .build();

        likeMovieRepository.save(likeMovie);

        Long countLikes = likeMovieRepository.countByMovieId(movieId);

        return countLikes;

    }

    public Long delete(Long movieId) {
        // 1) 현재 로그인 된 멤버의 ID를 가져온다.
        User loggedInUser = securityUtils.getLoginUser();

        LikeMovie like = likeMovieRepository.findByUserUserIdAndMovieId(loggedInUser.getUserId(), movieId);

        likeMovieRepository.delete(like);

        Long countLikes = likeMovieRepository.countByMovieId(movieId);

        return countLikes;
    }
}