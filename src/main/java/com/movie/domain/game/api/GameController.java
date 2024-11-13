package com.movie.domain.game.api;

import com.movie.domain.game.domain.Game;
import com.movie.domain.game.dto.request.CreateGameReqDto;
import com.movie.domain.game.dto.response.GetGameDetailResDto;
import com.movie.domain.game.service.GameService;
import com.movie.domain.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;
    private final PlayerService playerService;

    // 게임 방 생성
    @PostMapping("/game")
    public ResponseEntity<?> createGame(
            @RequestBody CreateGameReqDto reqDto
            ) {
        Game savedGame = gameService.save(reqDto);
        return ResponseEntity.ok(savedGame);
    }

    // 게임 방 참여
    @PostMapping("/game/{gameId}/join")
    public ResponseEntity<?> joinGame(
            @PathVariable Long gameId
    ) {

        playerService.save(gameId);

        return ResponseEntity.ok("게임 참가 완료");
    }

    // 게임 방 나가기
    @DeleteMapping("/game/{gameId}/exit")
    public ResponseEntity<?> exitGame(
            @PathVariable Long gameId
    ) {
        playerService.delete(gameId);

        return ResponseEntity.ok("게임 나가기 완료");
    }

    //게임 시작
    @PostMapping("/game/{gameId}/start")
    public ResponseEntity<?> startGame(
            @PathVariable Long gameId
    ) {
        gameService.update(gameId);

        return ResponseEntity.ok("게임 시작, 방 상태 변경");
    }

//    @GetMapping("/games")
//    public ResponseEntity<?> getGame() {
//
//    }



    @GetMapping("/game/{gameId}")
    public ResponseEntity<?> getGameById(
            @PathVariable Long gameId
    ) {
        GetGameDetailResDto getGameDetailResDto = gameService.getGameDetail(gameId);

        return ResponseEntity.ok(getGameDetailResDto);
    }

    @PostMapping("/game/{gameId}/ready")
    public ResponseEntity<?> readyGame(
            @PathVariable Long gameId
    ) {
        gameService.ready(gameId);

        return ResponseEntity.ok("준비 완료");
    }

}
