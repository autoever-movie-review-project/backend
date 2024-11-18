package com.movie.domain.pointHistory.api;

import com.movie.domain.pointHistory.dto.request.PointReqDto;
import com.movie.domain.pointHistory.dto.response.GetPointHistoryResDto;
import com.movie.domain.pointHistory.dto.response.GetPointResDto;
import com.movie.domain.pointHistory.service.PointHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PointHistoryController {
    private final PointHistoryService pointHistoryService;

    @GetMapping("/point/my/history")
    public ResponseEntity<?> getMyPointHistory() {
        List<GetPointHistoryResDto> pointHistorys = pointHistoryService.getHistorys();

        return ResponseEntity.ok(pointHistorys);
    }

    @PostMapping("/point/my")
    public ResponseEntity<?> addPoint(
            @RequestBody PointReqDto pointReqDto
            ) {
        GetPointResDto getPointResDto = pointHistoryService.add(pointReqDto);

        return ResponseEntity.ok(getPointResDto);
    }

}
