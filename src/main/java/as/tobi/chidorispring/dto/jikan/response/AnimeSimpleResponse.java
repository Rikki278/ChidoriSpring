package as.tobi.chidorispring.dto.jikan.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class AnimeSimpleResponse implements Serializable {
    private Long id;
    private String title;
    private List<String> genres;
}