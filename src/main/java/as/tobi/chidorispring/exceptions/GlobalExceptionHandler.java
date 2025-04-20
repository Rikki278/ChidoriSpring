package as.tobi.chidorispring.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InternalViolationException.class)
    public ResponseEntity<ErrorResponse> handleInternalViolation(InternalViolationException ex) {
        InternalViolationType type = ex.getType();
        ErrorResponse response = new ErrorResponse(type.getCode(), type.getMessage());
        return new ResponseEntity<>(response, type.getHttpStatus());
    }

    // Обработчик для ошибок валидации сущности (ConstraintViolationException)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse response = new ErrorResponse(
                3000,
                "Validation failed: " + errors
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Обработчик для ошибок валидации DTO (MethodArgumentNotValidException)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        ErrorResponse response = new ErrorResponse(
                3000,
                "Validation failed: " + errors
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Обработчик для всех остальных исключений (опционально)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        ErrorResponse response = new ErrorResponse(
                3000,
                "An unexpected error occurred: " + ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}