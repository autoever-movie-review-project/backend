package com.movie.domain.PointHistory.service;

import com.movie.domain.PointHistory.dao.PointHistoryRepository;
import com.movie.domain.PointHistory.domain.PointHistory;
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
    public List<PointHistory> getHistorys() {
        // 로그인 된 유저 불러오기
        User loggedInUser = securityUtils.getLoginUser();

        List<PointHistory> historys = pointHistoryRepository.findAllbyUserId(loggedInUser.getUserId());

        return historys;
    }
}
