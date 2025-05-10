package as.tobi.chidorispring.dto.auth;

import as.tobi.chidorispring.security.validation.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @ValidEmail(message = "Invalid email format")
    @NotBlank(message = "Email cannot be empty")
    private String email;
    private String password;
    private String username;
    private String firstName;
    private String lastName;
}