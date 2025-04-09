package as.tobi.chidorispring.exceptions;

import as.tobi.chidorispring.dto.exceptions.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InternalViolationException.class)
    public ResponseEntity<ErrorResponse> handleInternalViolation(InternalViolationException ex) {
        InternalViolationType type = ex.getType();
        ErrorResponse response = new ErrorResponse(type.getCode(), type.getMessage());
        return new ResponseEntity<>(response, type.getHttpStatus());
    }

}