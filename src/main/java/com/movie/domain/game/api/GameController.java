package com.movie.domain.game.api;

import com.movie.domain.game.domain.Game;
import com.movie.domain.game.dto.request.CreateGameReqDto;
import com.movie.domain.game.service.GameService;
import com.movie.domain.player.domain.Player;
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

        Player player = playerService.save(gameId);

        return ResponseEntity.ok(player);
    }

    // 게임 방 나가기
    @DeleteMapping("/game/{gameId}/exit")
    public ResponseEntity<?> exitGame(
            @PathVariable Long gameId
    ) {
        playerService.delete(gameId);

        return ResponseEntity.ok("게임 나가기 완료");
    }

}
