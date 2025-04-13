package as.tobi.chidorispring.dto.kitsu.response;

import as.tobi.chidorispring.dto.kitsu.PosterImage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AnimeFullInfoResponse implements Serializable {
    private String id;
    private String englishTitle;
    private String japaneseTitle;
    private String canonicalTitle;
    private String description;
    private PosterImage posterImages;
    private List<String> genres;
    private Integer episodeCount;
    private String status;
    private String ageRating;
    private String ageRatingGuide;
}