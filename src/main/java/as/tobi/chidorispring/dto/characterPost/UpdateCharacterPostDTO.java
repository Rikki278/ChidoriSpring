package as.tobi.chidorispring.dto.characterPost;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class UpdateCharacterPostDTO {
    private String characterName;
    private String anime;
    private List<String> animeGenre;
    private String description;
    private MultipartFile newCharacterImage;
    private Boolean shouldRemoveImage;
}
