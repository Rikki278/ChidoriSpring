package as.tobi.chidorispring.service;

import as.tobi.chidorispring.dto.jikan.*;
import as.tobi.chidorispring.dto.jikan.response.AnimeFullInfoResponse;
import as.tobi.chidorispring.dto.jikan.response.AnimeSimpleResponse;
import as.tobi.chidorispring.dto.jikan.response.JikanResponse;
import as.tobi.chidorispring.dto.jikan.response.PaginatedAnimeResponse;
import as.tobi.chidorispring.exceptions.AnimeViolationException;
import as.tobi.chidorispring.exceptions.AnimeViolationType;
import as.tobi.chidorispring.mapper.JikanAnimeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "jikanAnimeCache")
@Slf4j
public class JikanService {
    private final WebClient jikanWebClient;
    private final JikanAnimeMapper jikanAnimeMapper;

    // Fetches paginated anime with genres, caches result
    @Cacheable(key = "{'page', #page, 'size', #size}", unless = "#result == null || #result.data.isEmpty()")
    public Mono<PaginatedAnimeResponse> getAnimeWithGenres(int page, int size) {
        // Validate pagination parameters
        if (page <= 0 || size <= 0) {
            return Mono.error(new AnimeViolationException(AnimeViolationType.INVALID_PAGE_PARAMS));
        }

        return jikanWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/anime")
                        .queryParam("page", page)
                        .queryParam("limit", size)
                        .build())
                .retrieve()
                // Handle HTTP errors
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new AnimeViolationException(AnimeViolationType.JIKAN_API_ERROR)))
                .bodyToMono(JikanResponse.class)
                // Handle empty response
                .switchIfEmpty(Mono.error(new AnimeViolationException(AnimeViolationType.INVALID_JIKAN_RESPONSE_FORMAT)))
                // Map to response DTO
                .map(response -> jikanAnimeMapper.toPaginatedResponse(response, page, size));
    }

    // Searches anime by query across pages, caches result
    @Cacheable(key = "{'search', #query}", unless = "#result == null || #result.isEmpty()")
    public Mono<List<AnimeSimpleResponse>> searchAnimeAcrossAllPages(String query) {
        // Validate query
        if (query == null || query.trim().isEmpty()) {
            return Mono.error(new AnimeViolationException(AnimeViolationType.SEARCH_QUERY_EMPTY));
        }

        return searchAnimeRecursive(query, 1, new ArrayList<>());
    }

    // Recursively searches anime across pages
    private Mono<List<AnimeSimpleResponse>> searchAnimeRecursive(String query, int page,
                                                                 List<AnimeSimpleResponse> accumulatedResults) {
        final int PAGE_SIZE = 25; // Jikan API max limit

        return jikanWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/anime")
                        .queryParam("q", query)
                        .queryParam("page", page)
                        .queryParam("limit", PAGE_SIZE)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new AnimeViolationException(AnimeViolationType.JIKAN_API_ERROR)))
                .bodyToMono(JikanResponse.class)
                .switchIfEmpty(Mono.error(new AnimeViolationException(AnimeViolationType.INVALID_JIKAN_RESPONSE_FORMAT)))
                .flatMap(response -> {
                    // Map page results to DTOs
                    List<AnimeSimpleResponse> pageResults = response.getData().stream()
                            .map(jikanAnimeMapper::toAnimeSimpleResponse)
                            .collect(Collectors.toList());

                    accumulatedResults.addAll(pageResults);

                    // Continue if page is full and limit not reached
                    if (pageResults.size() == PAGE_SIZE && page < 10) {
                        return searchAnimeRecursive(query, page + 1, accumulatedResults);
                    }

                    // Handle no results
                    if (accumulatedResults.isEmpty()) {
                        return Mono.error(new AnimeViolationException(AnimeViolationType.ANIME_NOT_FOUND));
                    }

                    return Mono.just(accumulatedResults);
                });
    }

    // Searches anime by title with full details, caches result
    @Cacheable(key = "{'fullSearch', #partialTitle, 'limit', #limit}", unless = "#result == null || #result.isEmpty()")
    public Mono<List<AnimeFullInfoResponse>> getAnimeFullInfoByTitle(String partialTitle, int limit) {
        // Validate inputs
        if (partialTitle == null || partialTitle.trim().isEmpty()) {
            return Mono.error(new AnimeViolationException(AnimeViolationType.SEARCH_QUERY_EMPTY));
        }
        if (limit <= 0 || limit > 25) {
            return Mono.error(new AnimeViolationException(AnimeViolationType.INVALID_LIMIT));
        }

        return jikanWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/anime")
                        .queryParam("q", partialTitle)
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new AnimeViolationException(AnimeViolationType.JIKAN_API_ERROR)))
                .bodyToMono(JikanResponse.class)
                .switchIfEmpty(Mono.error(new AnimeViolationException(AnimeViolationType.INVALID_JIKAN_RESPONSE_FORMAT)))
                .flatMap(response -> {
                    // Filter matching anime
                    List<JikanAnimeData> matchingAnime = findMatchingAnime(response.getData(), partialTitle);

                    // Handle no matches
                    if (matchingAnime.isEmpty()) {
                        return Mono.error(new AnimeViolationException(AnimeViolationType.ANIME_NOT_FOUND));
                    }

                    // Map to full info DTOs
                    return Mono.just(matchingAnime.stream()
                            .map(jikanAnimeMapper::toAnimeFullInfoResponse)
                            .collect(Collectors.toList()));
                });
    }

    // Filters anime by title or Japanese title
    private List<JikanAnimeData> findMatchingAnime(List<JikanAnimeData> animeList, String partialTitle) {
        String searchTerm = partialTitle.toLowerCase();

        return animeList.stream()
                .filter(anime -> {
                    if (anime.getTitle() == null) {
                        return false;
                    }
                    boolean matchesMainTitle = anime.getTitle().toLowerCase().contains(searchTerm);
                    boolean matchesAltTitle = anime.getTitleJapanese() != null &&
                            anime.getTitleJapanese().toLowerCase().contains(searchTerm);
                    return matchesMainTitle || matchesAltTitle;
                })
                .collect(Collectors.toList());
    }

    // Clears all cache entries
    @CacheEvict(value = "jikanAnimeCache", allEntries = true, beforeInvocation = true)
    public void evictAllCache() {
        log.info("Evicting all jikanAnimeCache entries");
    }

    // Clears cache for specific page and size
    @CacheEvict(value = "jikanAnimeCache", key = "{'page', #page, 'size', #size}")
    public void evictPaginatedCache(int page, int size) {
        log.info("Evicting jikan cache for page {} size {}", page, size);
    }
}