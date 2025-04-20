package as.tobi.chidorispring.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "character_posts")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CharacterPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Character name cannot be empty")
    @Size(min = 2, max = 100, message = "Character name must be between 2 and 100 characters")
    private String characterName;

    @NotBlank(message = "Anime cannot be empty")
    @Size(min = 2, max = 100, message = "Anime name must be between 2 and 100 characters")
    private String anime;

    @NotBlank(message = "Anime genre cannot be empty")
    @Size(max = 50, message = "Anime genre must not exceed 50 characters")
    private String animeGenre;

    @NotBlank(message = "Description cannot be empty")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Column(name = "character_image_url")
    @Pattern(regexp = "^(https?://.*\\.(?:png|jpg|jpeg|gif))?$", message = "Character image URL must be a valid image URL")
    private String characterImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User cannot be null")
    private UserProfile user;

    @OneToMany(mappedBy = "characterPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LikesEntity> likes;

    @OneToMany(mappedBy = "characterPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> comments;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}