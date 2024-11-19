package com.movie.domain.recommendation.api;

import com.movie.domain.recommendation.dto.RecommendationReqDto;
import com.movie.domain.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recommendation")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping
    public ResponseEntity<Void> updatePreferences(@RequestBody RecommendationReqDto recommendationReqDto) {
        List<Long> movieIds = recommendationReqDto.getMovieIds();

        for (Long movieId : movieIds) {
            recommendationService.updatePreferences(movieId);
        }
        return ResponseEntity.ok().build();
    }
}
