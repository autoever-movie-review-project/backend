package com.movie.domain.movie.service;

import com.movie.domain.movie.domain.BoxOfficeMovieInfo;

import java.util.List;

public interface BoxOfficeService {
    List<BoxOfficeMovieInfo.MovieDetail> getDailyBoxOfficeList(String targetDate);
}
