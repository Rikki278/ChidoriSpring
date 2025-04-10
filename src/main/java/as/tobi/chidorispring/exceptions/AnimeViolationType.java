package as.tobi.chidorispring.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AnimeViolationType {
    INVALID_PAGE_PARAMS(20001, "Page and size parameters must be positive", HttpStatus.BAD_REQUEST),
    SEARCH_QUERY_EMPTY(20002, "Search query cannot be empty", HttpStatus.BAD_REQUEST),
    INVALID_LIMIT(20003, "Limit must be between 1 and 20", HttpStatus.BAD_REQUEST),
    ANIME_NOT_FOUND(20004, "No anime found matching the criteria", HttpStatus.NOT_FOUND),
    KITSU_API_ERROR(20005, "Kitsu API service unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    INVALID_RESPONSE_FORMAT(20006, "Invalid response format from Kitsu API", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}