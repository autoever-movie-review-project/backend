package com.movie.domain.socket.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long chatroomSeq;  // 채팅방 고유 번호
    private String sender;      // 메시지 보낸 사람
    private String content;     // 메시지 내용
    private String timestamp;   // 메시지 보낸 시간
}
