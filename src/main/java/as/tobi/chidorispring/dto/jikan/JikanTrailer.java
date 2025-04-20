package as.tobi.chidorispring.dto.jikan;

import as.tobi.chidorispring.dto.jikan.images.JikanTrailerImages;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class JikanTrailer implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty("youtube_id")
    private String youtubeId;
    @JsonProperty("url")
    private String url;
    @JsonProperty("embed_url")
    private String embedUrl;
    @JsonProperty("images") // Trailer thumbnail images
    private JikanTrailerImages images;
}