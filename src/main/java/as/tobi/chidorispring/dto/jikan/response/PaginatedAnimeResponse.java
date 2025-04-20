package as.tobi.chidorispring.dto.jikan.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class PaginatedAnimeResponse implements Serializable {
    private List<AnimeSimpleResponse> data;
    private int currentPage;
    private int pageSize;
    private int totalPages;
    private long totalItems;
}