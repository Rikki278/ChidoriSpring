package as.tobi.chidorispring.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum InternalViolationType {
    AUTHORIZATION_ERROR(10000, "User token validation is failed", HttpStatus.BAD_REQUEST),
    USER_ALREADY_EXISTS(10001, "User with this email already exists", HttpStatus.BAD_REQUEST),
    INCORRECT_PASSWORD(10002, "Incorrect password", HttpStatus.BAD_REQUEST),
    USER_IS_NOT_EXISTS(10003, "User is not exists", HttpStatus.BAD_REQUEST),
    AVATAR_NOT_FOUND(10004, "Avatar not found", HttpStatus.BAD_REQUEST),
    FILE_PROCESSING_ERROR(10005, "Error with file uploading", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(10006, "File is too large. File must be less than 1MB", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
