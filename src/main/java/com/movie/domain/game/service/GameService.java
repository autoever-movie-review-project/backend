package com.movie.domain.game.service;

import com.movie.domain.game.dao.GameRepository;
import com.movie.domain.game.domain.Game;
import com.movie.domain.game.domain.GameStatus;
import com.movie.domain.game.dto.request.CreateGameReqDto;
import com.movie.domain.game.dto.response.GetGameDetailResDto;
import com.movie.domain.game.exception.GameDuplicatedException;
import com.movie.domain.game.exception.GameIdNotFoundException;
import com.movie.domain.likeMovie.exception.LikeMovieDuplicateException;
import com.movie.domain.player.dao.PlayerRepository;
import com.movie.domain.player.domain.Player;
import com.movie.domain.player.dto.response.IsReadyPlayerResDto;
import com.movie.domain.player.dto.response.PlayerInfoDto;
import com.movie.domain.rank.dto.response.InGameRankDto;
import com.movie.domain.user.dao.UserRepository;
import com.movie.domain.user.domain.User;
import com.movie.domain.user.exception.EmailDuplicatedException;
import com.movie.domain.user.exception.UserIdNotFoundException;
import com.movie.global.exception.DuplicatedException;
import com.movie.global.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.movie.domain.game.constant.GameExceptionMessage.GAME_DUPLiCATE_ERROR;
import static com.movie.domain.user.constant.EmailExceptionMessage.EMAIL_DUPLICATED;

@RequiredArgsConstructor
@Service
public class GameService {
    private final GameRepository gameRepository;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    public Game save(CreateGameReqDto reqDto) {
        // 현재 로그인 된 유저를 가져온다.
        User loggedInUser = securityUtils.getLoginUser();


        // hostId가 이미 존재하는지 확인
        if (gameRepository.existsByHostId(loggedInUser.getUserId())) {
            throw new GameDuplicatedException(GAME_DUPLiCATE_ERROR.getMessage());
        }


        // 로그인 된 유저가 host인 Game 생성
        Game game = Game.builder()
                .hostId(loggedInUser.getUserId())
                .title(reqDto.title())
                .status(GameStatus.WAITING)
                .maxPlayer(reqDto.maxPlayer())
                .playerCount(1L)
                .createdAt(LocalDateTime.now())
                .build();

        // 저장
        gameRepository.save(game);

        // 유저의 Id를 불러온 뒤 Player entity 생성
        Player player = Player.builder()
                .game(game)
                .user(loggedInUser)
                .build();

        playerRepository.save(player);

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

    // 게임 대기실 상세 정보 가져오기
    @Transactional
    public GetGameDetailResDto getGameDetail(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(GameIdNotFoundException::new);

        //PlayerInfoDto를 만들기 위해 game에 참여하고 있는 player의 userId를 가져온 후
        // User를 list로 가져오기

        List<Player> player = playerRepository.findAllByGameId(gameId);

        List<User> users = new ArrayList<>();

        for(Player p : player) {
            User u = userRepository.findById(p.getUser().getUserId())
                    .orElseThrow(() -> new UserIdNotFoundException("user Id를 찾을 수 없습니다"));
            users.add(u);
        }

        List<PlayerInfoDto> playerInfoDto = new ArrayList<>();

        for(User u : users) {
            InGameRankDto inGameRankDto = InGameRankDto.builder()
                    .rankId(u.getRank().getRankId())
                    .rankImg(u.getRank().getRankImg())
                    .rankName(u.getRank().getRankName())
                    .build();

            playerInfoDto.add(PlayerInfoDto.of(u, inGameRankDto));
        }

        return GetGameDetailResDto.of(game, playerInfoDto);
    }

    @Transactional
    public List<IsReadyPlayerResDto> readyList(Long gameId) {
        // 현재 로그인 된 유저를 가져온다.
        User loggedInUser = securityUtils.getLoginUser();

        Long userId = loggedInUser.getUserId();

        Player player = playerRepository.findByUser_UserIdAndGameId(userId, gameId);

        // 현재 isReady 상태를 반전시킨다
        player.setIsReady(!player.isReady());

        playerRepository.save(player);

        List<IsReadyPlayerResDto> isReadyPlayerResDto = new ArrayList<>();

        List<Player> players = playerRepository.findAllByGameId(gameId);

        for(Player p : players) {

            isReadyPlayerResDto.add(IsReadyPlayerResDto.of(p.getUser(), p.isReady()));

        }

        return isReadyPlayerResDto;
    }

    @Transactional
    public Page<Game> getGameList(int page) {
        // 1) pageable 인터페이스 생성
        Pageable pageable = PageRequest.of(page, 8, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 2) 페이지 요청에 따른 모든 Game 조회
        Page<Game> games = gameRepository.findAll(pageable);

        return games;
    }


    // 게임 대기실 리스트 가져오기
//    @Transactional
//    public GetGameResDto getGame(Long gameId) {
//
//    }

}
