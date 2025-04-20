package as.tobi.chidorispring.dto.jikan;

import lombok.Data;

@Data
public class JikanPagination {
    private int lastVisiblePage;
    private JikanPaginationItems items;
}