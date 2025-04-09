package as.tobi.chidorispring.dto.kitsu;

import lombok.Data;

@Data
public class AnimeData {
    private String id;
    private String type;
    private AnimeAttributes attributes;
}