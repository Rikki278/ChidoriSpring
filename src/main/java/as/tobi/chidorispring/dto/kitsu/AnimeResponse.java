package as.tobi.chidorispring.dto.kitsu;

import lombok.Data;

import java.util.List;

@Data
public class AnimeResponse {
    private List<AnimeData> data;
}