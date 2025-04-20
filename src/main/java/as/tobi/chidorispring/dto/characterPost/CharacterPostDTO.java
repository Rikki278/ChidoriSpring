package as.tobi.chidorispring.dto.characterPost;


import as.tobi.chidorispring.dto.userProfile.UserProfileShortDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CharacterPostDTO {
    private Long id;
    private String characterName;
    private String anime;
    private List<String> animeGenre;
    private String description;
    private String characterImageUrl;
    private UserProfileShortDTO author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}