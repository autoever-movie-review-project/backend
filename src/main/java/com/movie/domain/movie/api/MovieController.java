package com.movie.domain.movie.api;

import com.movie.domain.movie.domain.BoxOfficeMovieInfo;
import com.movie.domain.movie.domain.TopReviewMovieInfo;
import com.movie.domain.movie.dto.response.MovieDetailResDto;
import com.movie.domain.movie.dto.response.MovieListResDto;
import com.movie.domain.movie.service.BoxOfficeService;
import com.movie.domain.movie.service.MovieService;
import com.movie.domain.movie.service.TmdbService;
import com.movie.domain.movie.service.TopReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movie")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final BoxOfficeService boxOfficeService;
    private final TopReviewService topReviewService;
    private final TmdbService tmdbService;

    // TmdbService 관련 API

    @PostMapping("/initialize")
    public ResponseEntity<Void> initializeMovies() {
        tmdbService.initializeMovies();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update")
    public ResponseEntity<Void> updateMovies() {
        tmdbService.updateNewMovies();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/search-and-save")
    public ResponseEntity<Void> searchAndSaveMovie(@RequestParam("id") Long id) {
        try {
            tmdbService.searchAndSaveMovie(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // MovieService 관련 API

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieDetailResDto> getMovieDetail(@PathVariable Long movieId) {
        return ResponseEntity.ok(movieService.findMovie(movieId));
    }

    @GetMapping("/genre")
    public ResponseEntity<List<MovieListResDto>> getMoviesByGenre(@RequestParam String genre) {
        return ResponseEntity.ok(movieService.getMoviesByMainGenre(genre));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<MovieListResDto>> getUpcomingMovies() {
        return ResponseEntity.ok(movieService.getUpcomingMovies());
    }

    @GetMapping("/popular")
    public ResponseEntity<List<MovieListResDto>> getPopularMovies() {
        return ResponseEntity.ok(movieService.getTopRatedMovies());
    }

    @GetMapping
    public ResponseEntity<List<MovieListResDto>> getAllMovies() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieListResDto>> searchMovies(
            @RequestParam String title,
            @RequestParam String genre) {
        return ResponseEntity.ok(movieService.searchMovies(title, genre));
    }

    // BoxOfficeService 관련 API

    @GetMapping("/box-office")
    public ResponseEntity<List<BoxOfficeMovieInfo.MovieDetail>> getBoxOfficeMovies(@RequestParam String targetDate) {
        return ResponseEntity.ok(boxOfficeService.getDailyBoxOfficeList(targetDate));
    }

    // TopReviewService 관련 API

    @GetMapping("/top-reviewed")
    public ResponseEntity<List<TopReviewMovieInfo.MovieDetail>> getTopReviewedMovies() {
        return ResponseEntity.ok(topReviewService.getTopRevieweMovieList());
    }
}
