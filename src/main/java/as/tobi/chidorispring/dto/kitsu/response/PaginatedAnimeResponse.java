package as.tobi.chidorispring.dto.kitsu.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PaginatedAnimeResponse implements Serializable {
    private List<AnimeSimpleResponse> data;
    private int currentPage;
    private int pageSize;
    private long totalItems;
    private int totalPages;
}