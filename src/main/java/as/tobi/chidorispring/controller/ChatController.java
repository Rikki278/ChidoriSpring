package as.tobi.chidorispring.controller;

import as.tobi.chidorispring.dto.chat.ChatMessageDTO;
import as.tobi.chidorispring.dto.chat.ChatRoomDTO;
import as.tobi.chidorispring.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/create/{otherUserId}")
    public ResponseEntity<ChatRoomDTO> createChatRoom(Authentication authentication,
                                                      @PathVariable Long otherUserId) {
        String email = authentication.getName();
        ChatRoomDTO chatRoom = chatService.createChatRoom(email, otherUserId);
        return ResponseEntity.ok(chatRoom);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDTO>> getUserChatRooms(Authentication authentication) {
        String email = authentication.getName();
        List<ChatRoomDTO> chatRooms = chatService.getUserChatRooms(email);
        return ResponseEntity.ok(chatRooms);
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getChatMessages(Authentication authentication,
                                                                @PathVariable Long roomId) {
        String email = authentication.getName();
        List<ChatMessageDTO> messages = chatService.getChatMessages(email, roomId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{roomId}/unread")
    public ResponseEntity<Long> getUnreadMessageCount(Authentication authentication,
                                                      @PathVariable Long roomId) {
        String email = authentication.getName();
        long count = chatService.getUnreadMessageCount(email, roomId);
        return ResponseEntity.ok(count);
    }
}