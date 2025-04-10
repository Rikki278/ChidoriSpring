package as.tobi.chidorispring.service;

import as.tobi.chidorispring.dto.kitsu.AnimeData;
import as.tobi.chidorispring.dto.kitsu.KitsuResponse;
import as.tobi.chidorispring.dto.kitsu.response.AnimeFullInfoResponse;
import as.tobi.chidorispring.dto.kitsu.response.AnimeSimpleResponse;
import as.tobi.chidorispring.dto.kitsu.response.PaginatedAnimeResponse;
import as.tobi.chidorispring.exceptions.AnimeViolationException;
import as.tobi.chidorispring.exceptions.AnimeViolationType;
import as.tobi.chidorispring.mapper.AnimeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KitsuService {
    // web client for making http requests to kitsu api
    private final WebClient kitsuWebClient;
    // converts raw api data to our application's format
    private final AnimeMapper animeMapper;

    // gets a page of anime with genre information
    public Mono<PaginatedAnimeResponse> getAnimeWithGenres(int page, int size) {
        // validate pagination parameters first
        if (page <= 0 || size <= 0) {
            // throw anime-specific exception for invalid params
            return Mono.error(new AnimeViolationException(AnimeViolationType.INVALID_PAGE_PARAMS));
        }

        // building the request uri with query parameters
        return kitsuWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/anime") // the api endpoint for anime
                        // how many items to return per page
                        .queryParam("page[limit]", size)
                        // where to start (page 2 starts at offset 20 if size=10)
                        .queryParam("page[offset]", (page - 1) * size)
                        // request to include category/genre information
                        .queryParam("include", "categories")
                        .build())
                // execute the request
                .retrieve()
                // handle api error responses
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new AnimeViolationException(AnimeViolationType.KITSU_API_ERROR)))
                // parse the response body
                .bodyToMono(KitsuResponse.class)
                // handle empty responses from api
                .switchIfEmpty(Mono.error(new AnimeViolationException(AnimeViolationType.INVALID_RESPONSE_FORMAT)))
                // transform the api response to our paginated format
                .map(response -> animeMapper.toPaginatedResponse(response, page, size));
    }

    // searches through all available pages to find matching anime
    public Mono<List<AnimeSimpleResponse>> searchAnimeAcrossAllPages(String query) {
        // validate search query isn't empty
        if (query == null || query.trim().isEmpty()) {
            return Mono.error(new AnimeViolationException(AnimeViolationType.SEARCH_QUERY_EMPTY));
        }

        // start the recursive search from page 1 with empty results
        return searchAnimeRecursive(query, 1, new ArrayList<>());
    }

    // helper method that searches page by page recursively
    private Mono<List<AnimeSimpleResponse>> searchAnimeRecursive(String query, int page,
                                                                 List<AnimeSimpleResponse> accumulatedResults) {
        // kitsu api allows maximum 20 items per request
        final int PAGE_SIZE = 20;

        // building the search request
        return kitsuWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/anime")
                        // text filter for searching
                        .queryParam("filter[text]", query)
                        .queryParam("page[limit]", PAGE_SIZE)
                        .queryParam("page[offset]", (page - 1) * PAGE_SIZE)
                        .queryParam("include", "categories")
                        .build())
                .retrieve()
                // handle api errors with anime-specific exception
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new AnimeViolationException(AnimeViolationType.KITSU_API_ERROR)))
                .bodyToMono(KitsuResponse.class)
                // handle invalid empty responses
                .switchIfEmpty(Mono.error(new AnimeViolationException(AnimeViolationType.INVALID_RESPONSE_FORMAT)))
                .flatMap(response -> {
                    // convert each anime in this page's results to our format
                    List<AnimeSimpleResponse> pageResults = response.getData().stream()
                            .map(anime -> animeMapper.toAnimeSimpleResponse(anime, response.getIncluded()))
                            .collect(Collectors.toList());

                    // add this page's results to our collection
                    accumulatedResults.addAll(pageResults);

                    // if we got a full page and haven't checked too many pages...
                    if (pageResults.size() == PAGE_SIZE && page < 10) {
                        // ...continue searching on the next page
                        return searchAnimeRecursive(query, page + 1, accumulatedResults);
                    }

                    // return error if no results found after searching
                    if (accumulatedResults.isEmpty()) {
                        return Mono.error(new AnimeViolationException(AnimeViolationType.ANIME_NOT_FOUND));
                    }

                    // otherwise return all results we've collected
                    return Mono.just(accumulatedResults);
                });
    }

    // finds anime by partial title match (like "nar" for "naruto")
    public Mono<List<AnimeFullInfoResponse>> getAnimeFullInfoByTitle(String partialTitle, int limit) {
        // validate search parameters first
        if (partialTitle == null || partialTitle.trim().isEmpty()) {
            return Mono.error(new AnimeViolationException(AnimeViolationType.SEARCH_QUERY_EMPTY));
        }
        if (limit <= 0 || limit > 20) {
            return Mono.error(new AnimeViolationException(AnimeViolationType.INVALID_LIMIT));
        }

        // building the search request
        return kitsuWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/anime")
                        .queryParam("filter[text]", partialTitle)
                        // maximum number of results to return
                        .queryParam("page[limit]", limit)
                        .queryParam("include", "categories")
                        .build())
                .retrieve()
                // handle api errors with anime-specific exception
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new AnimeViolationException(AnimeViolationType.KITSU_API_ERROR)))
                .bodyToMono(KitsuResponse.class)
                // handle invalid empty responses
                .switchIfEmpty(Mono.error(new AnimeViolationException(AnimeViolationType.INVALID_RESPONSE_FORMAT)))
                .flatMap(response -> {
                    // filter to only close matches
                    List<AnimeData> matchingAnime = findMatchingAnime(response.getData(), partialTitle);

                    // return error if no matches found
                    if (matchingAnime.isEmpty()) {
                        return Mono.error(new AnimeViolationException(AnimeViolationType.ANIME_NOT_FOUND));
                    }

                    // convert each match to our full info format
                    return Mono.just(matchingAnime.stream()
                            .map(anime -> animeMapper.toAnimeFullInfoResponse(anime, response.getIncluded()))
                            .collect(Collectors.toList()));
                });
    }

    // checks if anime titles contain our search term
    private List<AnimeData> findMatchingAnime(List<AnimeData> animeList, String partialTitle) {
        // clean up the search term for comparison
        String searchTerm = partialTitle.toLowerCase();

        return animeList.stream()
                // for each anime...
                .filter(anime -> {
                    // skip if anime attributes are invalid
                    if (anime.getAttributes() == null || anime.getAttributes().getCanonicalTitle() == null) {
                        return false;
                    }

                    // check the main title
                    String canonicalTitle = anime.getAttributes().getCanonicalTitle().toLowerCase();
                    boolean matchesCanonical = canonicalTitle.contains(searchTerm);

                    // check any alternate titles if they exist
                    boolean matchesAnyTitle = false;
                    if (anime.getAttributes().getTitles() != null) {
                        matchesAnyTitle = anime.getAttributes().getTitles().values().stream()
                                .filter(Objects::nonNull) // skip null titles
                                .anyMatch(title -> title.toLowerCase().contains(searchTerm));
                    }

                    // keep if either title matches
                    return matchesCanonical || matchesAnyTitle;
                })
                .collect(Collectors.toList());
    }
}