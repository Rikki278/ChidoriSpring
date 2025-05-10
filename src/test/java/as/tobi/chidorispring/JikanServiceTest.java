package as.tobi.chidorispring;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import as.tobi.chidorispring.service.JikanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import as.tobi.chidorispring.dto.jikan.JikanAnimeData;
import as.tobi.chidorispring.dto.jikan.response.AnimeFullInfoResponse;
import as.tobi.chidorispring.dto.jikan.response.AnimeSimpleResponse;
import as.tobi.chidorispring.dto.jikan.response.JikanResponse;
import as.tobi.chidorispring.dto.jikan.response.PaginatedAnimeResponse;
import as.tobi.chidorispring.exceptions.AnimeViolationException;
import as.tobi.chidorispring.mapper.JikanAnimeMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class JikanServiceTest {

    @Mock
    private WebClient jikanWebClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private ClientResponse clientResponse;

    @Mock
    private JikanAnimeMapper jikanAnimeMapper;

    @InjectMocks
    private JikanService jikanService;

    private JikanAnimeData testAnimeData;
    private AnimeSimpleResponse testAnimeSimpleResponse;
    private AnimeFullInfoResponse testAnimeFullInfoResponse;
    private JikanResponse testJikanResponse;
    private PaginatedAnimeResponse testPaginatedResponse;

    @BeforeEach
    void setUp() {
        // Подготовка тестовых данных
        testAnimeData = new JikanAnimeData();
        testAnimeData.setMalId(1L);
        testAnimeData.setTitle("Test Anime");
        testAnimeData.setTitleJapanese("テストアニメ");
        testAnimeData.setType("TV");
        testAnimeData.setEpisodes(12);
        testAnimeData.setStatus("Finished Airing");
        testAnimeData.setScore(8.5);
        testAnimeData.setSynopsis("Test Synopsis");

        testAnimeSimpleResponse = AnimeSimpleResponse.builder()
            .id(1L)
            .title("Test Anime")
            .genres(Arrays.asList("Action", "Adventure"))
            .build();

        testAnimeFullInfoResponse = AnimeFullInfoResponse.builder()
            .id(1L)
            .title("Test Anime")
            .japaneseTitle("テストアニメ")
            .type("TV")
            .episodes(12)
            .status("Finished Airing")
            .score(8.5)
            .synopsis("Test Synopsis")
            .genres(Arrays.asList("Action", "Adventure"))
            .build();

        testJikanResponse = new JikanResponse();
        testJikanResponse.setData(Arrays.asList(testAnimeData));

        testPaginatedResponse = PaginatedAnimeResponse.builder()
            .data(Arrays.asList(testAnimeSimpleResponse))
            .currentPage(1)
            .pageSize(10)
            .totalPages(1)
            .totalItems(1)
            .build();
    }

    @Test
    void getAnimeWithGenres_WithValidParams_ShouldReturnPaginatedResponse() {
        // Arrange
        when(jikanWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JikanResponse.class)).thenReturn(Mono.just(testJikanResponse));
        when(jikanAnimeMapper.toPaginatedResponse(any(JikanResponse.class), anyInt(), anyInt()))
            .thenReturn(testPaginatedResponse);

        // Act & Assert
        StepVerifier.create(jikanService.getAnimeWithGenres(1, 10))
            .expectNext(testPaginatedResponse)
            .verifyComplete();
    }

    @Test
    void getAnimeWithGenres_WithInvalidPage_ShouldThrowException() {
        // Act & Assert
        StepVerifier.create(jikanService.getAnimeWithGenres(0, 10))
            .expectError(AnimeViolationException.class)
            .verify();
    }

    @Test
    void searchAnimeAcrossAllPages_WithValidQuery_ShouldReturnAnimeList() {
        // Arrange
        when(jikanWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JikanResponse.class)).thenReturn(Mono.just(testJikanResponse));
        when(jikanAnimeMapper.toAnimeSimpleResponse(any(JikanAnimeData.class)))
            .thenReturn(testAnimeSimpleResponse);

        // Act & Assert
        StepVerifier.create(jikanService.searchAnimeAcrossAllPages("Test"))
            .expectNextMatches(list -> {
                assertEquals(1, list.size());
                assertEquals(testAnimeSimpleResponse.getId(), list.get(0).getId());
                return true;
            })
            .verifyComplete();
    }

    @Test
    void searchAnimeAcrossAllPages_WithEmptyQuery_ShouldThrowException() {
        // Act & Assert
        StepVerifier.create(jikanService.searchAnimeAcrossAllPages(""))
            .expectError(AnimeViolationException.class)
            .verify();
    }

    @Test
    void getAnimeFullInfoByTitle_WithValidTitle_ShouldReturnFullInfoList() {
        // Arrange
        when(jikanWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JikanResponse.class)).thenReturn(Mono.just(testJikanResponse));
        when(jikanAnimeMapper.toAnimeFullInfoResponse(any(JikanAnimeData.class)))
            .thenReturn(testAnimeFullInfoResponse);

        // Act & Assert
        StepVerifier.create(jikanService.getAnimeFullInfoByTitle("Test", 10))
            .expectNextMatches(list -> {
                assertEquals(1, list.size());
                assertEquals(testAnimeFullInfoResponse.getId(), list.get(0).getId());
                return true;
            })
            .verifyComplete();
    }

    @Test
    void getAnimeFullInfoByTitle_WithInvalidLimit_ShouldThrowException() {
        // Act & Assert
        StepVerifier.create(jikanService.getAnimeFullInfoByTitle("Test", 0))
            .expectError(AnimeViolationException.class)
            .verify();
    }

    @Test
    void getAnimeFullInfoByTitle_WithNoResults_ShouldThrowException() {
        // Arrange
        JikanResponse emptyResponse = new JikanResponse();
        emptyResponse.setData(List.of());

        when(jikanWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JikanResponse.class)).thenReturn(Mono.just(emptyResponse));

        // Act & Assert
        StepVerifier.create(jikanService.getAnimeFullInfoByTitle("Nonexistent", 10))
            .expectError(AnimeViolationException.class)
            .verify();
    }

    @Test
    void evictAllCache_ShouldClearCache() {
        // Act
        jikanService.evictAllCache();

        // Assert - проверяем, что метод выполнился без ошибок
        // В реальном приложении здесь можно было бы проверить состояние кэша
    }

    @Test
    void evictPaginatedCache_ShouldClearSpecificCache() {
        // Act
        jikanService.evictPaginatedCache(1, 10);

        // Assert - проверяем, что метод выполнился без ошибок
        // В реальном приложении здесь можно было бы проверить состояние кэша
    }
} 