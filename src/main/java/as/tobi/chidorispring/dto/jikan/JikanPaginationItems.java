package as.tobi.chidorispring.dto.jikan;

import lombok.Data;

@Data
public class JikanPaginationItems {
    private int total;
    private int count;
    private int perPage;
}