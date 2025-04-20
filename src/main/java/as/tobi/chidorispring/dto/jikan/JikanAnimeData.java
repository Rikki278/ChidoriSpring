package as.tobi.chidorispring.dto.jikan;

import as.tobi.chidorispring.dto.jikan.aired.JikanAired;
import as.tobi.chidorispring.dto.jikan.images.JikanImages;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class JikanAnimeData implements Serializable {
    @JsonProperty("mal_id")
    private Long malId;
    private String title;
    @JsonProperty("title_japanese")
    private String titleJapanese;
    private String synopsis;
    private List<JikanGenre> genres;
    private String type;
    private Integer episodes;
    private Double score;
    private String status;
    private JikanAired aired; // Represents the aired object (from/to dates)
    @JsonProperty("images") // Maps the images object from Jikan API
    private JikanImages images; // Added for image URLs
    @JsonProperty("trailer") // Added for trailer data
    private JikanTrailer trailer;
}