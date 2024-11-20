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

        // player 검증 : playerId가 이미 존재하는지 확인
        if (playerRepository.existsByUser_UserId(loggedInUser.getUserId())) {
            throw new IllegalArgumentException("이미 게임에 참여중입니다");
        }

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

        // playerCount 갱신
        Long playerCount = playerRepository.countByGameId(game.getId());
        game.setPlayerCount(playerCount);
        gameRepository.save(game);

        gameRepository.save(game);

    }

    @Transactional
    public GameStatusResDto delete(Long gameId) {
        // 로그인 된 유저 불러오기
        User loggedInUser = securityUtils.getLoginUser();

        // 유저의 id값으로 Player 불러오기
        Player player = playerRepository.findByUser_UserIdAndGameId(loggedInUser.getUserId(), gameId);

        // 해당 Player 삭제
        playerRepository.delete(player);

        // ** 게임 방 인원이 0명일 경우 게임 방 삭제 로직 **
        if (!playerRepository.existsByGameId(gameId)) {
            // gameId가 존재하는지 확인 후 삭제 시도
            if (gameRepository.existsById(gameId)) {
                Game game = gameRepository.findById(gameId).orElseThrow(GameIdNotFoundException::new);

                // playerCount 갱신
                Long playerCount = playerRepository.countByGameId(game.getId());
                game.setPlayerCount(playerCount);
                gameRepository.save(game);

                gameRepository.deleteById(gameId);

                return new GameStatusResDto(gameId, true, null); // 방 삭제된 경우
            }

        } else {
            // 나간 Player가 방장이면 방장권한 위임
            Game game = gameRepository.findById(gameId)
                    .orElseThrow(GameIdNotFoundException::new);

            if (loggedInUser.getUserId().equals(game.getHostId())) {
                // 무작위 선정을 위한 List<Player> 불러오기
                List<Player> players = playerRepository.findAllByGameId(gameId);

                Random random = new Random();
                Player hostPlayer = players.get(random.nextInt(players.size()));

                // game의 hostId 새로 설정
                game.setHostId(hostPlayer.getUser().getUserId());

                // playerCount 갱신
                Long playerCount = playerRepository.countByGameId(game.getId());
                game.setPlayerCount(playerCount);
                gameRepository.save(game);

                return new GameStatusResDto(gameId, false, game.getHostId()); // hostId가 바뀐 경우
            }

            // playerCount 갱신
            Long playerCount = playerRepository.countByGameId(game.getId());
            game.setPlayerCount(playerCount);
            gameRepository.save(game);
        }

        return new GameStatusResDto(gameId, false, null); // 기본 반환
    }


    public Long saveRandom() {
        // 로그인 된 유저 불러오기
        User loggedInUser = securityUtils.getLoginUser();

        // player 검증 : playerId가 이미 존재하는지 확인
        if (playerRepository.existsByUser_UserId(loggedInUser.getUserId())) {
            throw new IllegalArgumentException("이미 게임에 참여중입니다");
        }


        // status가 대기 상태이고, 인원이 다 차지 않은 game 리스트 불러오기
        List<Game> games = gameRepository.findAvailableGames(GameStatus.WAITING);

        if (!games.isEmpty()) {
            // 무작위로 하나의 게임 선택
            Random random = new Random();
            Game selectedGame = games.get(random.nextInt(games.size()));

            // 선택된 게임에 현재 유저 추가
            Player newPlayer = Player.builder()
                    .game(selectedGame)
                    .user(loggedInUser)
                    .isReady(false)
                    .score(0L) // 기본 점수 설정
                    .build();
            playerRepository.save(newPlayer);

            // 게임의 playerCount 증가
            // playerCount 갱신
            Long playerCount = playerRepository.countByGameId(selectedGame.getId());
            selectedGame.setPlayerCount(playerCount);

            gameRepository.save(selectedGame);
            return selectedGame.getId();
        } else {
            // 대기 중인 게임이 없을 때의 처리
            return null;
        }
    }
}
