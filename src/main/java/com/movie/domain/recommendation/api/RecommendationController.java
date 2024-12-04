package com.movie.domain.recommendation.api;

import com.movie.domain.recommendation.dto.RecommendationReqDto;
import com.movie.domain.recommendation.dto.RecommendationResDto;
import com.movie.domain.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommendation")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping
    public ResponseEntity<Void> initRecommendations(@RequestBody RecommendationReqDto recommendationReqDto) {
        //처음 3개 골라서 추천 생성
        recommendationService.initRecommendations(recommendationReqDto.getMovieIds(), 5);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping
    public ResponseEntity<List<RecommendationResDto>> findRecommendations() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(recommendationService.findRecommendations());
    }

    @PostMapping("/update")
    public ResponseEntity<String> forceUpdateRecommendations() {
        recommendationService.updateLoginUser();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
