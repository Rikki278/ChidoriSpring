package as.tobi.chidorispring.dto.kitsu;

import lombok.Data;

@Data
public class Included {
    private String id;
    private String type;
    private IncludedAttributes attributes;
}