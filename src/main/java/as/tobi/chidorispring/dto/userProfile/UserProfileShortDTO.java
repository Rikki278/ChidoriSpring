package as.tobi.chidorispring.dto.userProfile;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileShortDTO{
    private Long id;
    private String username;
    private String profileImageUrl;
}