package as.tobi.chidorispring.controller;

import as.tobi.chidorispring.dto.kitsu.response.AnimeFullInfoResponse;
import as.tobi.chidorispring.dto.kitsu.response.AnimeSimpleResponse;
import as.tobi.chidorispring.dto.kitsu.response.PaginatedAnimeResponse;
import as.tobi.chidorispring.service.KitsuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/anime")
@RequiredArgsConstructor
public class AnimeController {
    private final KitsuService kitsuService;

    @GetMapping
    public Mono<PaginatedAnimeResponse> getAnime(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return kitsuService.getAnimeWithGenres(page, size);
    }

    @GetMapping("/search")
    public Mono<List<AnimeSimpleResponse>> searchAnime(
            @RequestParam String query) {
        return kitsuService.searchAnimeAcrossAllPages(query);
    }

    @GetMapping("/search/full-info")
    public Mono<List<AnimeFullInfoResponse>> searchAnime(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit) {
        return kitsuService.getAnimeFullInfoByTitle(query, limit);
    }

    @PostMapping("/clear")
    public ResponseEntity<String> clearCache() {
        kitsuService.evictAllCache();
        return ResponseEntity.ok("Cache cleared successfully");
    }

}