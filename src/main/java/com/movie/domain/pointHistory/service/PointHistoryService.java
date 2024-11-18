package com.movie.domain.pointHistory.service;

import com.movie.domain.pointHistory.dao.PointHistoryRepository;
import com.movie.domain.pointHistory.domain.PointHistory;
import com.movie.domain.pointHistory.dto.request.PointReqDto;
import com.movie.domain.pointHistory.dto.response.GetPointHistoryResDto;
import com.movie.domain.pointHistory.dto.response.GetPointResDto;
import com.movie.domain.user.dao.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public List<GetPointHistoryResDto> getHistorys() {
        // 로그인 된 유저 불러오기
        User loggedInUser = securityUtils.getLoginUser();


        List<GetPointHistoryResDto> resDtos = new ArrayList<>();

        List<PointHistory> historys = pointHistoryRepository.findAllByUser_UserId(loggedInUser.getUserId());

        for(PointHistory history : historys) {
            resDtos.add(GetPointHistoryResDto.of(history));
        }
        return resDtos;
    }

    // 포인트 적립
    @Transactional
    public GetPointResDto add(PointReqDto pointReqDto) {
        User loggedInUser = securityUtils.getLoginUser();

        PointHistory pointHistory = PointHistory.builder()
                .user(loggedInUser)
                .points(pointReqDto.points())
                .description(pointReqDto.description())
                .build();

        pointHistoryRepository.save(pointHistory);

        // 적립될때마다 User의 points 속성 값 더해주기
        loggedInUser.updatepoints(pointReqDto.points());

        userRepository.save(loggedInUser);

        return GetPointResDto.of(loggedInUser, pointHistory);

    }
}
