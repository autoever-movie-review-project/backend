package com.movie.domain.likeMovie.service;

import com.movie.domain.game.domain.Game;
import com.movie.domain.likeMovie.dao.LikeMovieRepository;
import com.movie.domain.likeMovie.domain.LikeMovie;
import com.movie.domain.likeMovie.dto.response.LikeMovieResDto;
import com.movie.domain.likeMovie.exception.LikeMovieDuplicateException;
import com.movie.domain.movie.dao.MovieRepository;
import com.movie.domain.movie.domain.Movie;
import com.movie.domain.user.dao.UserRepository;
import com.movie.domain.user.domain.User;
import com.movie.global.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        if (likeMovieRepository.existsByUserUserIdAndMovieMovieId(loggedInUser.getUserId(), movieId)) {
            throw new LikeMovieDuplicateException("좋아요 중복");
        }

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow();

        LikeMovie likeMovie = LikeMovie.builder()
                .movie(movie)
                .user(loggedInUser)
                .build();

        likeMovieRepository.save(likeMovie);

        Long countLikes = likeMovieRepository.countByMovieMovieId(movieId);

        return countLikes;

    }

    public Long delete(Long movieId) {
        // 1) 현재 로그인 된 멤버의 ID를 가져온다.
        User loggedInUser = securityUtils.getLoginUser();

        LikeMovie like = likeMovieRepository.findByUserUserIdAndMovieMovieId(loggedInUser.getUserId(), movieId);

        likeMovieRepository.delete(like);

        Long countLikes = likeMovieRepository.countByMovieMovieId(movieId);

        return countLikes;
    }

    public Page<LikeMovieResDto> getLikes(int page) {
        // 1) 현재 로그인 된 멤버의 ID를 가져온다.
        User loggedInUser = securityUtils.getLoginUser();

        // 2) pageable 불러오기
        Pageable pageable = PageRequest.of(page, 8, Sort.by(Sort.Direction.DESC, "id"));

        // 3) 페이지 요청에 따른 모든 LikeMovie 조회
        Page<LikeMovie> likeMovies = likeMovieRepository.findByUserUserId(loggedInUser.getUserId(), pageable);

        // 4) LikeMovie를 LikeMovieResDto로 변환
        return likeMovies.map(LikeMovieResDto::of);
    }
}
