package com.movie.domain.player.service;

import com.movie.domain.player.dao.PlayerRepository;
import com.movie.domain.player.domain.Player;
import com.movie.domain.user.domain.User;
import com.movie.global.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final SecurityUtils securityUtils;

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

    public void delete(Long gameId) {
        // 로그인 된 유저 불러오기
        User loggedInUser = securityUtils.getLoginUser();

        // 유저의 id값으로 Player 불러오기
        Player player = playerRepository.findByUserId(loggedInUser.getUserId());

        playerRepository.delete(player);

    }
}
