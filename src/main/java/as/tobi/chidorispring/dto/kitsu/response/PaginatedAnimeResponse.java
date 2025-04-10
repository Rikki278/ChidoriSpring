package as.tobi.chidorispring.dto.kitsu.response;

import lombok.Data;

import java.util.List;

@Data
public class PaginatedAnimeResponse {
    private List<AnimeSimpleResponse> data;
    private int currentPage;
    private int pageSize;
    private long totalItems;
    private int totalPages;
}