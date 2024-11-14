package com.movie.domain.movie.dto.response;

import com.movie.domain.movie.dto.request.TmdbMovieInfo;
import lombok.Data;

import java.util.List;

@Data
public class TmdbMovieResDto {
    private List<TmdbMovieInfo> results;
    private int page;
    private int totalResults;
    private int totalPages;
}
