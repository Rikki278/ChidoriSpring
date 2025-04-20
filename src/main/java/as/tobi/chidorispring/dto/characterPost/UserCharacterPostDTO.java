package as.tobi.chidorispring.dto.characterPost;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserCharacterPostDTO {
    private Long id;
    private String characterName;
    private String anime;
    private List<String> animeGenre;
    private String description;
    private String characterImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
