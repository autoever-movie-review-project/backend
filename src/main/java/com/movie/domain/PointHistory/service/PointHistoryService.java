package com.movie.domain.PointHistory.service;

import com.movie.domain.PointHistory.dao.PointHistoryRepository;
import com.movie.domain.PointHistory.domain.PointHistory;
import com.movie.domain.PointHistory.dto.response.GetPointResDto;
import com.movie.domain.user.domain.User;
import com.movie.global.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointHistoryService {
    private final PointHistoryRepository pointHistoryRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public List<GetPointResDto> getHistorys() {
        // 로그인 된 유저 불러오기
        User loggedInUser = securityUtils.getLoginUser();


        List<GetPointResDto> resDtos = new ArrayList<>();

        List<PointHistory> historys = pointHistoryRepository.findAllByUser_UserId(loggedInUser.getUserId());

        for(PointHistory history : historys) {
            resDtos.add(GetPointResDto.of(history));
        }
        return resDtos;
    }
}
