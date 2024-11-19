package com.movie.domain.recommendation.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecommendationReqDto {
    private List<Long> movieIds;
}
