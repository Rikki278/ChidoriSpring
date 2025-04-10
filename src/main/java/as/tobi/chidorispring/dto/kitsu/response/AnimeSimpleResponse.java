package as.tobi.chidorispring.dto.kitsu.response;

import lombok.Data;

import java.util.List;

@Data
public class AnimeSimpleResponse {
    private String title;
    private List<String> genres;
}