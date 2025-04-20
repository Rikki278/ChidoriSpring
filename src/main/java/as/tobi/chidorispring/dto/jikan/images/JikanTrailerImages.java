package as.tobi.chidorispring.dto.jikan.images;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class JikanTrailerImages implements Serializable {
    @JsonProperty("image_url")
    private String imageUrl; // Default thumbnail
    @JsonProperty("small_image_url")
    private String smallImageUrl; // Small thumbnail
    @JsonProperty("medium_image_url")
    private String mediumImageUrl; // Medium thumbnail
    @JsonProperty("large_image_url")
    private String largeImageUrl; // Large thumbnail
    @JsonProperty("maximum_image_url")
    private String maximumImageUrl; // Maximum resolution thumbnail
}