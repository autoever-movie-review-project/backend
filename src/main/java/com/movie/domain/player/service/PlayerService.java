package com.movie.domain.player.service;

import com.movie.domain.game.dao.GameRepository;
import com.movie.domain.game.domain.Game;
import com.movie.domain.game.domain.GameStatus;
import com.movie.domain.game.dto.response.GameStatusResDto;
import com.movie.domain.game.exception.GameIdNotFoundException;
import com.movie.domain.player.dao.PlayerRepository;
import com.movie.domain.player.domain.Player;
import com.movie.domain.player.exception.GameAlreadyStartedException;
import com.movie.domain.player.exception.GameRoomFullException;
import com.movie.domain.user.domain.User;
import com.movie.global.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final SecurityUtils securityUtils;
    private final GameRepository gameRepository;

    @Transactional
    public void save(Long gameId) {
        // 로그인 된 유저 불러오기
        User loggedInUser = securityUtils.getLoginUser();

        // 게임 방의 인원이 다 찼는지에 대한 검증
        // 게임이 시작 상태인지에 대한 검증
        Game game = gameRepository.findById(gameId)
                .orElseThrow(GameIdNotFoundException::new);

        // 게임 상태 검증: 게임이 이미 시작된 경우 예외 발생
        if (game.getStatus().equals(GameStatus.STARTED)) {
            throw new GameAlreadyStartedException("게임이 이미 시작되었습니다. 입장할 수 없습니다.");
        }

        // 인원 검증: 최대 인원에 도달한 경우 예외 발생
        if (game.getPlayerCount() >= game.getMaxPlayer()) {
            throw new GameRoomFullException("게임 방의 인원이 다 찼습니다. 입장할 수 없습니다.");
        }

        // 유저의 Id를 불러온 뒤 Player entity 생성
        Player player = Player.builder()
                .game(game)
                .user(loggedInUser)
                .build();

        playerRepository.save(player);

        game.setPlayerCountUp();

        gameRepository.save(game);

    }

    @Transactional
    public GameStatusResDto delete(Long gameId) {
        // 로그인 된 유저 불러오기
        User loggedInUser = securityUtils.getLoginUser();

        // 유저의 id값으로 Player 불러오기
        Player player = playerRepository.findByUser_UserId(loggedInUser.getUserId());

        // 해당 Player 삭제
        playerRepository.delete(player);

        // ** 게임 방 인원이 0명일 경우 게임 방 삭제 로직 **

        // playerRepository에서 gameId의 값에 해당하는 Player가 없을 경우 방 삭제
        if (!playerRepository.existsByGameId(gameId)) {
            // gameId가 존재하는지 확인 후 삭제 시도
            if (gameRepository.existsById(gameId)) {
                gameRepository.deleteById(gameId);
                return new GameStatusResDto(gameId, true, null); // 방 삭제된 경우
            }

        } else {
            // 나간 Player가 방장이면 방장권한 위임
            Game game = gameRepository.findById(gameId)
                    .orElseThrow(GameIdNotFoundException::new);

            if(loggedInUser.getUserId().equals(game.getHostId())) {
                // 무작위 선정을 위한 List<Player> 불러오기
                List<Player> players = playerRepository.findAllByGameId(gameId);

                Random random = new Random();
                Player hostPlayer = players.get(random.nextInt(players.size()));

                // game의 hostId 새로 설정
                game.setHostId(hostPlayer.getUser().getUserId());

                return new GameStatusResDto(gameId, false, game.getHostId()); // hostId가 바뀐 경우
            }

            // Game의 참여중인 player의 수 -1 하는 로직
            Game joinGame = gameRepository.findById(gameId)
                    .orElseThrow(GameIdNotFoundException::new);

            joinGame.setPlayerCountDown();
            gameRepository.save(joinGame);


        }

        return new GameStatusResDto(gameId, false, null); // 기본 반환

    }

}