package com.movie.domain.movie.api;

import com.movie.domain.movie.service.TmdbService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movie")
@RequiredArgsConstructor
public class MovieController {
    private final TmdbService tmdbService;

    @PostMapping("/initialize")
    public ResponseEntity<String> initializeMovies() {
        tmdbService.initializeMovies(); // TMDB에서 최대 5000개 영화를 가져와서 DB에 추가
        return ResponseEntity.ok("Movie database initialized with TMDB data.");
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateMovies() {
        tmdbService.updateNewMovies(); // 새로 추가된 영화 업데이트
        return ResponseEntity.ok("Movie database updated with latest TMDB data.");
    }
}
