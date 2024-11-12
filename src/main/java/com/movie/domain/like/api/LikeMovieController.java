package com.movie.domain.like.api;

import com.movie.domain.like.dto.response.LikeMovieResDto;
import com.movie.domain.like.service.LikeMovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class LikeMovieController {
    private final LikeMovieService likeService;

    @PostMapping("/like/movie/{movieId}")
    public ResponseEntity<?> saveLike(
            @PathVariable Long movieId
    ) {
        Long countLikes = likeService.save(movieId);

        return ResponseEntity.ok(countLikes);
    }

//    @DeleteMapping("/like/{likeMovieId}/movie")
//    public ResponseEntity<?> deleteLike(
//            @PathVariable Long tilId
//    ) {
//        LikeDeleteResponse likeDeleteResponse = likeService.delete(tilId);
//
//        return ResponseEntity.ok(likeDeleteResponse);
//    }
}
