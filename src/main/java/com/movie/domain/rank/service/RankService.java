package com.movie.domain.rank.service;

import com.movie.domain.rank.dao.RankRepository;
import com.movie.domain.rank.dto.response.GetMyGradeDto;
import com.movie.domain.user.domain.User;
import com.movie.global.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankService {
    private final RankRepository rankRepository;
    private final SecurityUtils securityUtils;

    public GetMyGradeDto getGrade() {
        // 현재 로그인 된 유저를 가져온다.
        User loggedInUser = securityUtils.getLoginUser();

        return GetMyGradeDto.of(loggedInUser);
    }
}
