package as.tobi.chidorispring.exceptions;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InternalViolationException extends RuntimeException {

    private InternalViolationType type;

    public InternalViolationException(InternalViolationType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "InternalViolationException{" +
                "type=" + type +
                '}';
    }
}
