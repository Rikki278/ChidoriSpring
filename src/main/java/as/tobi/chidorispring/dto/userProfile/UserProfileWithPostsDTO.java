package as.tobi.chidorispring.dto.userProfile;

import as.tobi.chidorispring.dto.characterPost.UserCharacterPostDTO;
import as.tobi.chidorispring.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserProfileWithPostsDTO implements Serializable {
    private Long id;
    private String username;
    private String email;
    private String bio;
    private UserRole role;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<UserCharacterPostDTO> characterPosts;
    private List<UserCharacterPostDTO> favoritePosts;
}