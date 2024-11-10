package com.movie.domain.game.exception;

import com.movie.global.exception.NotFoundException;

public class GameIdNotFoundException extends NotFoundException {
    public GameIdNotFoundException() {super("[ERROR] 해당 gameId는 존재하지 않습니다.");}
}
