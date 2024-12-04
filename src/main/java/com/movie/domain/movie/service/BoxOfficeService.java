package com.movie.domain.movie.service;

import com.movie.domain.movie.domain.BoxOfficeMovieInfo;
import com.movie.domain.movie.dto.response.BoxOfficeListResDto;

import java.util.List;

public interface BoxOfficeService {
    List<BoxOfficeListResDto> findDailyBoxOfficeList();
}
