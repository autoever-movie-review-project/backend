package com.movie.domain.likeMovie.dto.response;

import com.movie.domain.likeMovie.domain.LikeMovie;


public record LikeMovieResDto(
        Long movieId,
        String mainImage
) {
    public static LikeMovieResDto of(LikeMovie likeMovie) {
        return new LikeMovieResDto(
                likeMovie.getMovie().getMovieId(),
                likeMovie.getMovie().getMainImg()
        );
    }
}
