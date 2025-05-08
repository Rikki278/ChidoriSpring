package as.tobi.chidorispring.repository;

import as.tobi.chidorispring.entity.ChatMessage;
import as.tobi.chidorispring.entity.ChatRoom;
import as.tobi.chidorispring.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomOrderBySentAtAsc(ChatRoom chatRoom);

    long countByChatRoomAndSenderNotAndIsReadFalse(ChatRoom chatRoom, UserProfile sender);
}