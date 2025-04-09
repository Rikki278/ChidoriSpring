package as.tobi.chidorispring.exceptions;

import lombok.Getter;

@Getter
public class InternalViolationException extends RuntimeException {

    private final InternalViolationType type;

    public InternalViolationException(InternalViolationType type) {
        super(type.getMessage());
        this.type = type;
    }

}
