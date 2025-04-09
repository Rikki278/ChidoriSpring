package as.tobi.chidorispring.service;

import as.tobi.chidorispring.dto.kitsu.AnimeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KitsuService {

    private final RestTemplate restTemplate;
    private static final String KITSU_API_URL = "https://kitsu.io/api/edge/anime?page[limit]=10";

    @Autowired
    public KitsuService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<String> getAnimeTitles() {
        // Setting headlines
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, "application/vnd.api+json"); // We indicate the desired Accept

        // Create httpentity with headlines
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // We perform a GET request with the right headlines
        AnimeResponse response = restTemplate.exchange(
                KITSU_API_URL,
                HttpMethod.GET,
                entity,
                AnimeResponse.class
        ).getBody();

        if (response == null || response.getData() == null) {
            return Collections.emptyList();
        }

        return response.getData().stream()
                .filter(anime -> anime.getAttributes() != null && anime.getAttributes().getTitles() != null)
                .map(anime -> anime.getAttributes().getTitles().getEn() != null
                        ? anime.getAttributes().getTitles().getEn()
                        : anime.getAttributes().getTitles().getEn_jp())
                .collect(Collectors.toList());
    }
}