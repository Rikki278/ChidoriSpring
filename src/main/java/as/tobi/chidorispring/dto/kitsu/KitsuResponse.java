package as.tobi.chidorispring.dto.kitsu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KitsuResponse {
    private List<AnimeData> data;
    private List<Included> included;
    private Meta meta;
}