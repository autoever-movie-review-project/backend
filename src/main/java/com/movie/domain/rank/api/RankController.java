package com.movie.domain.rank.api;

import com.movie.domain.rank.dto.response.GetMyGradeDto;
import com.movie.domain.rank.service.RankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RankController {
    private final RankService rankService;

    // 사용자 등급 조회
    @GetMapping("/rank/my")
    public ResponseEntity<?> getMyGrade() {
        GetMyGradeDto getMyGradeDto = rankService.getGrade();

        return ResponseEntity.ok(getMyGradeDto);
    }
}
