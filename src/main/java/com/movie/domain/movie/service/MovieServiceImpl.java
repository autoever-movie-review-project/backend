package com.movie.domain.movie.service;

import com.movie.domain.likeMovie.dao.LikeMovieRepository;
import com.movie.domain.movie.dao.MovieRepository;
import com.movie.domain.movie.dto.response.MovieDetailResDto;
import com.movie.domain.movie.dto.response.MovieListResDto;
import com.movie.domain.movie.exception.MovieNotFoundException;
import com.movie.domain.user.domain.User;
import com.movie.global.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.movie.domain.movie.constant.MovieExceptionMessage.MOVIE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {
    private final SecurityUtils securityUtils;
    private final MovieRepository movieRepository;
    private final LikeMovieRepository likeMovieRepository;

    private static final int PAGE_SIZE = 10;

    @Override
    @Transactional(readOnly = true)
    public MovieDetailResDto findMovie(Long movieId) {
        User user = securityUtils.getLoginUser();

        // movieId로 영화를 검색하고 응답 객체 생성
        return movieRepository.findByMovieId(movieId)
                .map(movie -> {
                    // 유저가 해당 영화를 좋아요 했는지 확인
                    boolean liked = likeMovieRepository.existsByUser_UserIdAndMovie_MovieId(user.getUserId(), movieId);
                    return MovieDetailResDto.entityToResDto(movie, liked);
                })
                .orElseThrow(() -> new MovieNotFoundException(MOVIE_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieListResDto> findMoviesByMainGenre(String genre, int page) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);
        return movieRepository.findByFilteredGenre(genre, pageRequest).stream()
                .filter(movie -> movie.getBackdropImg() != null)
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieListResDto> findUpcomingMovies(int page) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);
        return movieRepository.findByReleaseDateAfterOrderByReleaseDateAsc(LocalDate.now(), pageRequest).stream()
                .filter(movie -> movie.getBackdropImg() != null)
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieListResDto> findTopRatedMovies(int page) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);
        return movieRepository.findTopRatedMovies(pageRequest).stream()
                .filter(movie -> movie.getBackdropImg() != null)
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieListResDto> findAllMovies(int page) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);
        return movieRepository.findAll(pageRequest).stream()
                .filter(movie -> movie.getBackdropImg() != null)
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieListResDto> searchMovies(String keyword, int page) {
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }

        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);
        return movieRepository.searchMoviesByKeyword(keyword, pageRequest).stream()
                .filter(movie -> movie.getBackdropImg() != null)
                .map(MovieListResDto::entityToResDto)
                .collect(Collectors.toList());
    }
}
