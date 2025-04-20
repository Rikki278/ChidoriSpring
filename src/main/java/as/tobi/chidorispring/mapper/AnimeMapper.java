package as.tobi.chidorispring.mapper;

import as.tobi.chidorispring.dto.kitsu.AnimeData;
import as.tobi.chidorispring.dto.kitsu.Included;
import as.tobi.chidorispring.dto.kitsu.KitsuResponse;
import as.tobi.chidorispring.dto.kitsu.PosterImage;
import as.tobi.chidorispring.dto.kitsu.response.AnimeFullInfoResponse;
import as.tobi.chidorispring.dto.kitsu.response.AnimeSimpleResponse;
import as.tobi.chidorispring.dto.kitsu.response.PaginatedAnimeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Component
@Slf4j
public class AnimeMapper {

    // converts kitsu api response to paginated response with anime list and metadata
    public PaginatedAnimeResponse toPaginatedResponse(KitsuResponse kitsuResponse, int page, int size) {
        // map each anime data to simple response dto
        List<AnimeSimpleResponse> animeList = kitsuResponse.getData().stream()
                .map(anime -> toAnimeSimpleResponse(anime, kitsuResponse.getIncluded()))
                .collect(Collectors.toList());

        // build pagination response
        PaginatedAnimeResponse response = new PaginatedAnimeResponse();
        response.setData(animeList);;
        response.setCurrentPage(page);
        response.setPageSize(size);
        response.setTotalItems(kitsuResponse.getMeta().getCount());
        response.setTotalPages((int) Math.ceil((double) kitsuResponse.getMeta().getCount() / size));

        return response;
    }

    // converts anime data to simplified response with title and genres
    public AnimeSimpleResponse toAnimeSimpleResponse(AnimeData animeData, List<Included> included) {
        AnimeSimpleResponse dto = new AnimeSimpleResponse();
        dto.setId(animeData.getId());
        dto.setTitle(animeData.getAttributes().getCanonicalTitle());

        // safely map genres if relationships exist
        if (animeData.getRelationships() != null
                && animeData.getRelationships().getCategories() != null
                && animeData.getRelationships().getCategories().getData() != null) {

            dto.setGenres(animeData.getRelationships().getCategories().getData().stream()
                    .map(categoryLink -> findGenreName(included, categoryLink.getId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        } else {
            dto.setGenres(Collections.emptyList());
        }

        return dto;
    }

    // converts anime data to full detailed response with all available information
    public AnimeFullInfoResponse toAnimeFullInfoResponse(AnimeData animeData, List<Included> included) {
        AnimeFullInfoResponse response = new AnimeFullInfoResponse();

        // basic info mapping
        response.setId(animeData.getId());
        response.setCanonicalTitle(animeData.getAttributes().getCanonicalTitle());
        response.setDescription(animeData.getAttributes().getSynopsis());

        // map localized titles if available
        if (animeData.getAttributes().getTitles() != null) {
            response.setEnglishTitle(animeData.getAttributes().getTitles().get("en"));
            response.setJapaneseTitle(animeData.getAttributes().getTitles().get("ja_jp"));
        }

        // map poster images in all available sizes
        if (animeData.getAttributes().getPosterImage() != null) {
            PosterImage images = new PosterImage();
            images.setTiny(animeData.getAttributes().getPosterImage().getTiny());
            images.setSmall(animeData.getAttributes().getPosterImage().getSmall());
            images.setMedium(animeData.getAttributes().getPosterImage().getMedium());
            images.setLarge(animeData.getAttributes().getPosterImage().getLarge());
            images.setOriginal(animeData.getAttributes().getPosterImage().getOriginal());
            response.setPosterImages(images);
        }

        // map genres from included relationships
        if (animeData.getRelationships() != null
                && animeData.getRelationships().getCategories() != null
                && animeData.getRelationships().getCategories().getData() != null) {

            response.setGenres(animeData.getRelationships().getCategories().getData().stream()
                    .map(link -> findGenreName(included, link.getId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        } else {
            response.setGenres(Collections.emptyList());
        }

        // additional metadata mapping
        response.setEpisodeCount(animeData.getAttributes().getEpisodeCount());
        response.setStatus(animeData.getAttributes().getStatus());
        response.setAgeRating(animeData.getAttributes().getAgeRating());
        response.setAgeRatingGuide(animeData.getAttributes().getAgeRatingGuide());

        return response;
    }

    // helper method to find genre name by category id in included data
    private String findGenreName(List<Included> included, String categoryId) {
        if (included == null) return null;

        return included.stream()
                // filter valid category items
                .filter(item -> item != null && "categories".equals(item.getType()))
                // match by category id
                .filter(item -> categoryId.equals(item.getId()))
                .findFirst()
                // prefer title attribute, fallback to name
                .map(item -> {
                    if (item.getAttributes() == null) return null;
                    return item.getAttributes().getTitle() != null ?
                            item.getAttributes().getTitle() :
                            item.getAttributes().getName();
                })
                .orElse(null);
    }
}
