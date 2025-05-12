package as.tobi.chidorispring.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RecommendationResponse {
    private List<String> recommendedAnime;
    private List<String> recommendedGenres;
    private List<String> recommendedCharacters;

    @JsonCreator
    public RecommendationResponse(
        @JsonProperty("recommendedAnime") List<String> recommendedAnime,
        @JsonProperty("recommendedGenres") List<String> recommendedGenres,
        @JsonProperty("recommendedCharacters") List<String> recommendedCharacters
    ) {
        this.recommendedAnime = recommendedAnime;
        this.recommendedGenres = recommendedGenres;
        this.recommendedCharacters = recommendedCharacters;
    }
} 