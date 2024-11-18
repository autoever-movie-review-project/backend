package com.movie.domain.game.constant;

public enum GameExceptionMessage {
    GAME_DUPLiCATE_ERROR("이미 게임에 참여중입니다");

    private final String message;
    GameExceptionMessage(String message) {this.message = message;}
    public String getMessage() {return message;}
}
