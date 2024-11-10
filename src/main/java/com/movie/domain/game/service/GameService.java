package com.movie.domain.game.service;

import com.movie.domain.game.dao.GameRepository;
import com.movie.domain.game.domain.Game;
import com.movie.domain.game.domain.GameStatus;
import com.movie.domain.game.dto.request.CreateGameReqDto;
import com.movie.domain.game.exception.GameIdNotFoundException;
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
                .status(GameStatus.WAITING)
                .maxPlayer(reqDto.maxPlayer())
                .playerCount(1)
                .build();

        // 저장
        gameRepository.save(game);

        return game;
    }

    // 게임 상태 업데이트
    public void update(Long gameId) {
        // gameId에 해당하는 Game을 가져온다
        Game game = gameRepository.findById(gameId)
                .orElseThrow(GameIdNotFoundException::new);

        // start로 status 변경
        game.gameStart();

        // 다시 저장
        gameRepository.save(game);

    }
}
