package as.tobi.chidorispring.dto.jikan.response;

import as.tobi.chidorispring.dto.jikan.JikanAnimeData;
import as.tobi.chidorispring.dto.jikan.JikanPagination;
import lombok.Data;

import java.util.List;

@Data
public class JikanResponse {
    private List<JikanAnimeData> data;
    private JikanPagination pagination;
}