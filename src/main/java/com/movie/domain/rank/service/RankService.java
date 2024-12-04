package com.movie.domain.rank.service;

import com.movie.domain.rank.dao.RankRepository;
import com.movie.domain.rank.dto.response.GetMyGradeDto;
import com.movie.domain.user.dao.UserRepository;
import com.movie.domain.user.domain.User;
import com.movie.global.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RankService {
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;

    @Transactional
    public GetMyGradeDto getGrade() {
        // 현재 로그인 된 유저를 가져온다.
        User loggedInUser = securityUtils.getLoginUser();

        // 유저의 포인트 점수를 가져온다.
        Integer userPoints = loggedInUser.getPoints();

        // 전체 유저 수와 현재 유저보다 낮은 점수를 가진 유저 수 계산
        Long totalUsers = userRepository.count();
        Long usersBelow = userRepository.countByPointsLessThan(userPoints);

        // 상위 퍼센트 계산
        double rankPercent = 100-(((double) usersBelow / totalUsers) * 100);

        String formattedPercent = String.format("%.2f", rankPercent);

        return GetMyGradeDto.of(loggedInUser, formattedPercent);
    }
}
