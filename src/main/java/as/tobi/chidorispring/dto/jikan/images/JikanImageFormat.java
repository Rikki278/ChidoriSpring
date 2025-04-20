package as.tobi.chidorispring.dto.jikan.images;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class JikanImageFormat implements Serializable {
    @JsonProperty("image_url")
    private String imageUrl;
    @JsonProperty("small_image_url")
    private String smallImageUrl;
    @JsonProperty("large_image_url")
    private String largeImageUrl;
}
