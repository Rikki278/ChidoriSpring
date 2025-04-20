package as.tobi.chidorispring.dto.auth;

import as.tobi.chidorispring.security.validation.ValidEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @ValidEmail(message = "Invalid email format")
    @NotBlank(message = "Email cannot be empty")
    private String email;
    @NotBlank(message = "Password cannot be empty")
    private String password;
}