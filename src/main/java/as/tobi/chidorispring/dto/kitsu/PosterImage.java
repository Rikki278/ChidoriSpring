package as.tobi.chidorispring.dto.kitsu;

import lombok.Data;

import java.io.Serializable;

@Data
public class PosterImage implements Serializable {
    private String tiny;
    private String small;
    private String medium;
    private String large;
    private String original;
}
