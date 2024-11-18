package com.movie.domain.socket.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long chatroomSeq;     // 채팅방 고유 번호
    private String sender;         // 메시지 보낸 사람
    private String content;        // 메시지 내용
    private String timestamp;      // 메시지 보낸 시간 (String 형식으로 저장)

    public ChatMessage(Long chatroomSeq, String sender, String content, String timestamp) {
        this.chatroomSeq = chatroomSeq;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }
}
