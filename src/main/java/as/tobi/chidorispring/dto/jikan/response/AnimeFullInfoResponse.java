package as.tobi.chidorispring.dto.jikan.response;

import as.tobi.chidorispring.dto.jikan.JikanTrailer;
import as.tobi.chidorispring.dto.jikan.images.JikanImages;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class AnimeFullInfoResponse implements Serializable {
    private Long id;
    private String title;
    private String japaneseTitle;
    private String synopsis;
    private List<String> genres;
    private String type;
    private Integer episodes;
    private Double score;
    private String status;
    private String aired; // Formatted string from JikanAired (e.g., "2021-01-08 to 2021-01-08")
    private JikanImages images; // JPG and WebP image URLs
    private JikanTrailer trailer; // Trailer details (YouTube ID, URL, embed URL)
}