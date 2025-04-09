package as.tobi.chidorispring.dto.userProfile;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AvatarResponse {
    private Long userId;
    private String username;
    private String imageName;
    private String imageBase64; // Картинка в формате base64
    private String mimeType;
}