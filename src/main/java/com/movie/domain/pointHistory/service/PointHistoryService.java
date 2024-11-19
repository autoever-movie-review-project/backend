package com.movie.domain.pointHistory.service;

import com.movie.domain.pointHistory.dao.PointHistoryRepository;
import com.movie.domain.pointHistory.domain.PointHistory;
import com.movie.domain.pointHistory.dto.request.PointReqDto;
import com.movie.domain.pointHistory.dto.response.GetPointHistoryResDto;
import com.movie.domain.pointHistory.dto.response.GetPointResDto;
import com.movie.domain.rank.dao.RankRepository;
import com.movie.domain.rank.domain.Rank;
import com.movie.domain.user.dao.UserRepository;
import com.movie.domain.user.domain.User;
import com.movie.global.exception.NotFoundException;
import com.movie.global.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.movie.domain.review.constant.ReviewExceptionMessage.RANK_NOT_FOUND_FOR_POINTS;

@Service
@RequiredArgsConstructor
public class PointHistoryService {
    private final PointHistoryRepository pointHistoryRepository;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final RankRepository rankRepository;

    @Transactional
    public List<GetPointHistoryResDto> getHistorys() {
        // 로그인 된 유저 불러오기
        User loggedInUser = securityUtils.getLoginUser();


        List<GetPointHistoryResDto> resDtos = new ArrayList<>();

        List<PointHistory> historys = pointHistoryRepository.findAllByUser_UserId(loggedInUser.getUserId());

        for (PointHistory history : historys) {
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

        //랭크 확인해 줘야합니당
        Rank newRank = rankRepository.findByPointsBetweenStartAndEnd(loggedInUser.getPoints())
                .orElseThrow(() -> new NotFoundException(RANK_NOT_FOUND_FOR_POINTS.getMessage()));

        if (loggedInUser.getRank() == null || !newRank.getRankId().equals(loggedInUser.getRank().getRankId())) {
            loggedInUser.updateRank(newRank);
        }

        userRepository.save(loggedInUser);

        return GetPointResDto.of(loggedInUser, pointHistory);
    }
}
