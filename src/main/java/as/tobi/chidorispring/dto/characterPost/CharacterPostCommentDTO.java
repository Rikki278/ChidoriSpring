package as.tobi.chidorispring.dto.characterPost;

import as.tobi.chidorispring.dto.userProfile.UserProfileShortCommentDTO;
import as.tobi.chidorispring.dto.userProfile.UserProfileShortDTO;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class CharacterPostCommentDTO implements Serializable {
    private Long id;
    private String content;
    private UserProfileShortCommentDTO author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}