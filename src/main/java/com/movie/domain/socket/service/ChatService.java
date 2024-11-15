package com.movie.domain.socket.service;

import com.movie.domain.socket.dao.ChatMessageRepository;
import com.movie.domain.socket.domain.ChatMessage;
import com.movie.domain.socket.dto.request.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage saveMessage(ChatMessageDto messageDto) {
        ChatMessage chatMessage = new ChatMessage(
                messageDto.getChatroomSeq(),
                messageDto.getSender(),
                messageDto.getContent(),
                messageDto.getTimestamp()
        );
        return chatMessageRepository.save(chatMessage);
    }
}