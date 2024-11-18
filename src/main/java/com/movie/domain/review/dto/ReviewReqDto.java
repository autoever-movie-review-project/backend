package com.movie.domain.review.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewReqDto {

    private Long movieId;
    private String content;
    private double rating;
}
