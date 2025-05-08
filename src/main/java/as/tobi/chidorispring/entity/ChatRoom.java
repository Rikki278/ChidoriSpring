package as.tobi.chidorispring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_room")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user1_id", nullable = false)
    private UserProfile user1;

    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = false)
    private UserProfile user2;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    // Уникальный индекс для предотвращения дублирования комнат
    @Column(name = "room_identifier", unique = true)
    private String roomIdentifier;

    @PrePersist
    public void generateRoomIdentifier() {
        // Генерируем уникальный идентификатор комнаты на основе ID пользователей
        long user1Id = user1.getId();
        long user2Id = user2.getId();
        this.roomIdentifier = user1Id < user2Id
                ? user1Id + "_" + user2Id
                : user2Id + "_" + user1Id;
    }
}