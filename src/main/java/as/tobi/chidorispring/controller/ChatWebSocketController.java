package as.tobi.chidorispring.controller;

import as.tobi.chidorispring.dto.chat.ChatMessageDTO;
import as.tobi.chidorispring.entity.ChatRoom;
import as.tobi.chidorispring.entity.UserProfile;
import as.tobi.chidorispring.service.ChatService;
import as.tobi.chidorispring.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class ChatWebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserService userService;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate,
                                   ChatService chatService,
                                   UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.userService = userService;
    }

    @MessageMapping("/chat/{roomId}/send")
    public void sendMessage(@DestinationVariable Long roomId,
                            @Payload ChatMessageDTO messageDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        // Сохраняем сообщение в БД
        ChatMessageDTO savedMessage = chatService.sendMessage(email, roomId, messageDTO.getContent());

        // Отправляем сообщение всем подписчикам комнаты
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, savedMessage);

        // Отправляем уведомление о новом сообщении конкретному пользователю
        UserProfile sender = userService.getUserByEmail(email);
        UserProfile recipient = getOtherUserInChatRoom(roomId, sender);

        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/notifications",
                Map.of(
                        "type", "NEW_MESSAGE",
                        "roomId", roomId,
                        "senderId", sender.getId(),
                        "senderName", sender.getUsername(),
                        "message", savedMessage.getContent()
                )
        );
    }

    @SubscribeMapping("/chat/{roomId}/messages")
    public List<ChatMessageDTO> subscribeToChatMessages(@DestinationVariable Long roomId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return chatService.getChatMessages(email, roomId);
    }

    private UserProfile getOtherUserInChatRoom(Long roomId, UserProfile currentUser) {
        ChatRoom chatRoom = chatService.getChatRoomById(roomId);
        return chatRoom.getUser1().equals(currentUser)
                ? chatRoom.getUser2()
                : chatRoom.getUser1();
    }
}