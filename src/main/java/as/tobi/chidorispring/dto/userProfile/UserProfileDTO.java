package as.tobi.chidorispring.dto.userProfile;

import as.tobi.chidorispring.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileDTO {
    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private String bio;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}