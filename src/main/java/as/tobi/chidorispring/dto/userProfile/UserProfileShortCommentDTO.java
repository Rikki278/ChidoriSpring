package as.tobi.chidorispring.dto.userProfile;


import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class UserProfileShortCommentDTO implements Serializable {
    private Long id;
    private String username;
    private String email;
    private String profileImageUrl;
}