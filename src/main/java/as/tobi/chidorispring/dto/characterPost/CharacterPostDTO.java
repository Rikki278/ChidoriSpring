package as.tobi.chidorispring.dto.characterPost;


import as.tobi.chidorispring.dto.userProfile.UserProfileShortDTO;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CharacterPostDTO implements Serializable {
    private Long id;
    private String characterName;
    private String anime;
    private List<String> animeGenre;
    private String description;
    private String characterImageUrl;
    private UserProfileShortDTO author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long likeCount; // Added for like count
    private long commentCount; // Added for comment count
    private boolean isFavorited; // Added to indicate if the post is favorited by the current user
}