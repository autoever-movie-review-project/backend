package com.movie.domain.PointHistory.api;

import com.movie.domain.PointHistory.domain.PointHistory;
import com.movie.domain.PointHistory.service.PointHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PointHistoryController {
    private final PointHistoryService pointHistoryService;

    @GetMapping("/point/my")
    public ResponseEntity<?> getMyPointHistory() {
        List<PointHistory> pointHistorys = pointHistoryService.getHistorys();

        return ResponseEntity.ok(pointHistorys);
    }

}
