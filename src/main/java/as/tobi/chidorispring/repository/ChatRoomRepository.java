package as.tobi.chidorispring.repository;

import as.tobi.chidorispring.entity.ChatMessage;
import as.tobi.chidorispring.entity.ChatRoom;
import as.tobi.chidorispring.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByRoomIdentifier(String roomIdentifier);

    @Query("SELECT cr FROM ChatRoom cr WHERE (cr.user1 = :user AND cr.user2 = :otherUser) OR (cr.user1 = :otherUser AND cr.user2 = :user)")
    Optional<ChatRoom> findChatRoomBetweenUsers(@Param("user") UserProfile user, @Param("otherUser") UserProfile otherUser);

    List<ChatRoom> findByUser1OrUser2OrderByCreatedAtDesc(UserProfile user1, UserProfile user2);
}

