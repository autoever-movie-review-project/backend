package com.movie.domain.movie.service;

import com.movie.domain.movie.domain.TopReviewMovieInfo;
import com.movie.domain.movie.dto.response.TopReviewedMoviesResDto;

import java.util.List;

public interface TopReviewService {
    List<TopReviewedMoviesResDto> findTopRevieweMovieList();
}
