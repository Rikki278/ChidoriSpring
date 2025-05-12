package as.tobi.chidorispring.dto;

import java.util.List;

import lombok.Data;

@Data
public class RecommendationRequest {
    private List<PostData> favoritePosts;
    
    @Data
    public static class PostData {
        private String characterName;
        private String anime;
        private List<String> animeGenre;
    }
} 