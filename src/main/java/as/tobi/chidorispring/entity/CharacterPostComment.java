package as.tobi.chidorispring.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "character_post_comments")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CharacterPostComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Comment cannot be empty")
    @Size(max = 500, message = "Comment must not exceed 500 characters")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_post_id", nullable = false)
    private CharacterPost characterPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile user;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}