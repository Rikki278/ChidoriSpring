package as.tobi.chidorispring.dto.userProfile;

import lombok.Data;

@Data
public class UpdateUserProfileDTO {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}
