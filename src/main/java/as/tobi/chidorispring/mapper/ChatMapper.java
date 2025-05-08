package as.tobi.chidorispring.mapper;

import as.tobi.chidorispring.dto.chat.ChatMessageDTO;
import as.tobi.chidorispring.dto.chat.ChatRoomDTO;
import as.tobi.chidorispring.entity.ChatMessage;
import as.tobi.chidorispring.entity.ChatRoom;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

    public ChatMessageDTO toChatMessageDto(ChatMessage message) {
        if (message == null) {
            return null;
        }

        return ChatMessageDTO.builder()
                .id(message.getId())
                .roomId(message.getChatRoom() != null ? message.getChatRoom().getId() : null)
                .senderId(message.getSender() != null ? message.getSender().getId() : null)
                .senderName(message.getSender() != null ? message.getSender().getUsername() : null)
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .isRead(message.isRead())
                .build();
    }

    public ChatRoomDTO toChatRoomDto(ChatRoom room) {
        if (room == null) {
            return null;
        }

        return ChatRoomDTO.builder()
                .id(room.getId())
                .user1Id(room.getUser1() != null ? room.getUser1().getId() : null)
                .user1Name(room.getUser1() != null ? room.getUser1().getUsername() : null)
                .user2Id(room.getUser2() != null ? room.getUser2().getId() : null)
                .user2Name(room.getUser2() != null ? room.getUser2().getUsername() : null)
                .createdAt(room.getCreatedAt())
                .build();
    }

}