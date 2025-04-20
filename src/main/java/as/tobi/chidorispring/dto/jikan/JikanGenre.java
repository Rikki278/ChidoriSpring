package as.tobi.chidorispring.dto.jikan;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class JikanGenre {
    @JsonProperty("mal_id")
    private Long malId;
    private String name;
}
