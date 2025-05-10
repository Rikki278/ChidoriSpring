package as.tobi.chidorispring.dto.userProfile;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserProfileDTO {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
}
