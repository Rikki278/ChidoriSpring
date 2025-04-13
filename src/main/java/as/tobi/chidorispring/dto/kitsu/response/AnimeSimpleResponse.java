package as.tobi.chidorispring.dto.kitsu.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AnimeSimpleResponse implements Serializable {
    private String id;
    private String title;
    private List<String> genres;
}