package as.tobi.chidorispring.service;

import as.tobi.chidorispring.dto.chat.ChatMessageDTO;
import as.tobi.chidorispring.dto.chat.ChatRoomDTO;
import as.tobi.chidorispring.entity.*;
import as.tobi.chidorispring.exceptions.InternalViolationException;
import as.tobi.chidorispring.exceptions.InternalViolationType;
import as.tobi.chidorispring.mapper.ChatMapper;
import as.tobi.chidorispring.repository.ChatMessageRepository;
import as.tobi.chidorispring.repository.ChatRoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;
    private final ChatMapper chatMapper;

    public ChatService(ChatRoomRepository chatRoomRepository,
                       ChatMessageRepository chatMessageRepository,
                       UserService userService,
                       ChatMapper chatMapper) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userService = userService;
        this.chatMapper = chatMapper;
    }



    @Transactional
    public ChatRoomDTO createChatRoom(String currentUserEmail, Long otherUserId) {
        UserProfile currentUser = userService.getUserByEmail(currentUserEmail);
        UserProfile otherUser = userService.loadUserById(otherUserId);

        // Проверяем, существует ли уже чат между этими пользователями
        Optional<ChatRoom> existingRoom = chatRoomRepository.findChatRoomBetweenUsers(currentUser, otherUser);
        if (existingRoom.isPresent()) {
            return chatMapper.toChatRoomDto(existingRoom.get());
        }

        // Создаем новую комнату
        ChatRoom chatRoom = ChatRoom.builder()
                .user1(currentUser)
                .user2(otherUser)
                .build();

        chatRoom = chatRoomRepository.save(chatRoom);
        return chatMapper.toChatRoomDto(chatRoom);
    }

    public List<ChatRoomDTO> getUserChatRooms(String email) {
        UserProfile user = userService.getUserByEmail(email);
        List<ChatRoom> chatRooms = chatRoomRepository.findByUser1OrUser2OrderByCreatedAtDesc(user, user);
        return chatRooms.stream()
                .map(chatMapper::toChatRoomDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatMessageDTO sendMessage(String senderEmail, Long roomId, String content) {
        UserProfile sender = userService.getUserByEmail(senderEmail);
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new InternalViolationException(InternalViolationType.CHAT_ROOM_NOT_FOUND));

        // Проверяем, что отправитель является участником чата
        if (!chatRoom.getUser1().equals(sender) && !chatRoom.getUser2().equals(sender)) {
            throw new InternalViolationException(InternalViolationType.CHAT_ROOM_NOT_FOUND);
        }

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(content)
                .build();

        message = chatMessageRepository.save(message);
        return chatMapper.toChatMessageDto(message);
    }

    public List<ChatMessageDTO> getChatMessages(String userEmail, Long roomId) {
        UserProfile user = userService.getUserByEmail(userEmail);
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new InternalViolationException(InternalViolationType.CHAT_ROOM_NOT_FOUND));

        // Проверяем, что пользователь является участником чата
        if (!chatRoom.getUser1().equals(user) && !chatRoom.getUser2().equals(user)) {
            throw new InternalViolationException(InternalViolationType.CHAT_ROOM_NOT_FOUND);
        }

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomOrderBySentAtAsc(chatRoom);

        // Помечаем сообщения как прочитанные
        messages.stream()
                .filter(m -> !m.getSender().equals(user) && !m.isRead())
                .forEach(m -> {
                    m.setRead(true);
                    chatMessageRepository.save(m);
                });

        return messages.stream()
                .map(chatMapper::toChatMessageDto)
                .collect(Collectors.toList());
    }

    public long getUnreadMessageCount(String userEmail, Long roomId) {
        UserProfile user = userService.getUserByEmail(userEmail);
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new InternalViolationException(InternalViolationType.CHAT_ROOM_NOT_FOUND));

        return chatMessageRepository.countByChatRoomAndSenderNotAndIsReadFalse(chatRoom, user);
    }

    public ChatRoom getChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new InternalViolationException(InternalViolationType.CHAT_ROOM_NOT_FOUND));
    }

}