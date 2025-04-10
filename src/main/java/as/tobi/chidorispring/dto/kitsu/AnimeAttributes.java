package as.tobi.chidorispring.dto.kitsu;

import lombok.Data;

import java.util.Map;

@Data
public class AnimeAttributes {
    private String canonicalTitle;
    private String synopsis;
    private PosterImage posterImage;
    private Map<String, String> titles;
    private Integer episodeCount;
    private String status;
    private String ageRating;
    private String ageRatingGuide;
}