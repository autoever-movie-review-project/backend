package com.movie.domain.game.service;

import com.movie.domain.game.dao.GameRepository;
import com.movie.domain.game.domain.Game;
import com.movie.domain.game.dto.request.CreateGameReqDto;
import com.movie.domain.user.domain.User;
import com.movie.global.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class GameService {
    private final GameRepository gameRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public Game save(CreateGameReqDto reqDto) {
        // 현재 로그인 된 유저를 가져온다.
        User loggedInUser = securityUtils.getLoginUser();


        // 로그인 된 유저가 host인 Game 생성
        Game game = Game.builder()
                .hostId(loggedInUser.getUserId())
                .title(reqDto.title())
                .status("대기중")
                .maxPlayer(reqDto.maxPlayer())
                .playerCount(1)
                .build();

        // 저장
        gameRepository.save(game);

        return game;
    }

}
