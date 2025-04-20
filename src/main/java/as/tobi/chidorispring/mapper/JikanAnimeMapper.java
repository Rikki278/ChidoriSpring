package as.tobi.chidorispring.mapper;

import as.tobi.chidorispring.dto.jikan.*;

import as.tobi.chidorispring.dto.jikan.response.AnimeFullInfoResponse;
import as.tobi.chidorispring.dto.jikan.response.AnimeSimpleResponse;
import as.tobi.chidorispring.dto.jikan.response.JikanResponse;
import as.tobi.chidorispring.dto.jikan.response.PaginatedAnimeResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JikanAnimeMapper {

    public PaginatedAnimeResponse toPaginatedResponse(JikanResponse response, int page, int size) {
        List<AnimeSimpleResponse> animeList = response.getData().stream()
                .map(this::toAnimeSimpleResponse)
                .collect(Collectors.toList());

        return PaginatedAnimeResponse.builder()
                .data(animeList)
                .currentPage(page)
                .pageSize(size)
                .totalPages(response.getPagination().getLastVisiblePage())
                .totalItems(response.getPagination().getItems().getTotal())
                .build();
    }

    public AnimeSimpleResponse toAnimeSimpleResponse(JikanAnimeData anime) {
        return AnimeSimpleResponse.builder()
                .id(anime.getMalId())
                .title(anime.getTitle())
                .genres(anime.getGenres().stream()
                        .map(JikanGenre::getName)
                        .collect(Collectors.toList()))
                .build();
    }

    public AnimeFullInfoResponse toAnimeFullInfoResponse(JikanAnimeData anime) {
        // Convert JikanAired to a formatted string
        String airedString = anime.getAired() != null
                ? (anime.getAired().getFrom() != null ? anime.getAired().getFrom() : "") +
                (anime.getAired().getTo() != null ? " to " + anime.getAired().getTo() : "")
                : "Unknown";

        return AnimeFullInfoResponse.builder()
                .id(anime.getMalId())
                .title(anime.getTitle())
                .japaneseTitle(anime.getTitleJapanese())
                .synopsis(anime.getSynopsis())
                .genres(anime.getGenres().stream()
                        .map(JikanGenre::getName)
                        .collect(Collectors.toList()))
                .type(anime.getType())
                .episodes(anime.getEpisodes())
                .score(anime.getScore())
                .status(anime.getStatus())
                .aired(airedString)
                .images(anime.getImages())
                .trailer(anime.getTrailer()) // Direct mapping of trailer
                .build();
    }
}