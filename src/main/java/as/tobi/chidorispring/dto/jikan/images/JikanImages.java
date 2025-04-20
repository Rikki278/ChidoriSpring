package as.tobi.chidorispring.dto.jikan.images;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class JikanImages implements Serializable {
    @JsonProperty("jpg")
    private JikanImageFormat jpg;
    @JsonProperty("webp")
    private JikanImageFormat webp;
}