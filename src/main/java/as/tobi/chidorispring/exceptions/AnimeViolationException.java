package as.tobi.chidorispring.exceptions;

import lombok.Getter;

@Getter
public class AnimeViolationException extends RuntimeException {
    private final AnimeViolationType type;

    public AnimeViolationException(AnimeViolationType type) {
        super(type.getMessage());
        this.type = type;
    }
}