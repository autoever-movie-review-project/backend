package com.movie.domain.gameroom.service;

import com.movie.domain.gameroom.dao.GameRoomRepository;
import com.movie.domain.gameroom.domain.GameRoom;
import com.movie.domain.gameroom.dto.request.CreateGameRoomReqDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class GameRoomService {
    private final GameRoomRepository gameRoomRepository;

    @Transactional
    public GameRoom save(CreateGameRoomReqDto reqDto) {
        // 현재 로그인 된 유저의 Email을 가져온다.
//        User loggedInUser = securityUtils.getLoginUser();


        GameRoom gameRoom = GameRoom.builder()
                .hostId(1L)
                .title(reqDto.title())
                .status("대기중")
                .maxPlayer(reqDto.maxPlayer())
                .playerCount(1)
                .build();

        // 저장
        gameRoomRepository.save(gameRoom);

        return gameRoom;
    }
}
