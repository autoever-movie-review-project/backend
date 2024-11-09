package com.movie.domain.gameroom.api;

import com.movie.domain.gameroom.domain.GameRoom;
import com.movie.domain.gameroom.dto.request.CreateGameRoomReqDto;
import com.movie.domain.gameroom.service.GameRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GameRoomController {
    private final GameRoomService gameRoomService;

    @PostMapping("/game")
    public ResponseEntity<?> createGameRoom(
            @RequestBody CreateGameRoomReqDto reqDto
            ) {
        GameRoom savedGameRoom = gameRoomService.save(reqDto);
        return ResponseEntity.ok(savedGameRoom);
    }

}
