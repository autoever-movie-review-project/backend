package com.movie.domain.movie.service;

import com.movie.domain.movie.domain.TopReviewMovieInfo;

import java.util.List;

public interface TopReviewService {
    List<TopReviewMovieInfo.MovieDetail> getTopRevieweMovieList();
}
