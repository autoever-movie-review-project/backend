package com.movie.domain.player.service;

import com.movie.domain.game.dao.GameRepository;
import com.movie.domain.player.dao.PlayerRepository;
import com.movie.domain.player.domain.Player;
import com.movie.domain.user.domain.User;
import com.movie.global.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final SecurityUtils securityUtils;
    private final GameRepository gameRepository;

    @Transactional
    public Player save(Long gameId) {
        // 로그인 된 유저 불러오기
        User loggedInUser = securityUtils.getLoginUser();

        // 유저의 Id를 불러온 뒤 Player entity 생성
        Player player = Player.builder()
                .gameId(gameId)
                .userId(loggedInUser.getUserId())
                .build();

        playerRepository.save(player);

        return player;
    }

    @Transactional
    public void delete(Long gameId) {
        // 로그인 된 유저 불러오기
        User loggedInUser = securityUtils.getLoginUser();

        // 유저의 id값으로 Player 불러오기
        Player player = playerRepository.findByUserId(loggedInUser.getUserId());

        playerRepository.delete(player);

        // ** 게임 방 인원이 0명일 경우 게임 방 삭제 로직 **

        // playerRepository에서 gameId의 값에 해당하는 Player가 없을 경우 방 삭제
        if(!playerRepository.existByGameId(gameId)) {

            gameRepository.deleteById(gameId);

        };
    }
}
