package as.tobi.chidorispring.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import as.tobi.chidorispring.dto.RecommendationRequest;
import as.tobi.chidorispring.dto.RecommendationResponse;
import as.tobi.chidorispring.entity.CharacterPost;
import as.tobi.chidorispring.repository.CharacterPostRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
    
    private final CharacterPostRepository characterPostRepository;
    private final ChatGptService chatGptService;

    public List<CharacterPost> getRecommendationsForUser(Long userId) {
        log.info("Getting recommendations for user with ID: {}", userId);
        
        // get the last 10 selected user posts
        List<CharacterPost> favoritePosts = characterPostRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
        log.info("Found {} favorite posts for user", favoritePosts.size());
        
        // Принудительная загрузка жанров
        for (CharacterPost post : favoritePosts) {
            List<String> directGenres = characterPostRepository.findGenresByCharacterPostId(post.getId());
            log.error("Diagnostic - Post ID: {}, Direct Genre Query Result: {}", 
                post.getId(), directGenres);
            
            // forcefully install genres if they are not loaded
            if (post.getAnimeGenre() == null || post.getAnimeGenre().isEmpty()) {
                post.setAnimeGenre(directGenres);
            }
            
            log.error("Diagnostic - Post ID: {}, Loaded Genres: {}", 
                post.getId(), 
                post.getAnimeGenre() != null ? post.getAnimeGenre() : "NULL");
        }

        // If posts are less than 10, add empty
        while (favoritePosts.size() < 10) {
            CharacterPost emptyPost = CharacterPost.builder()
                .characterName("Unknown")
                .anime("Unknown")
                .animeGenre(List.of("Unknown"))
                .build();
            favoritePosts.add(emptyPost);
        }
        log.info("Total posts after filling (including empty ones): {}", favoritePosts.size());

        // convert to the ChatGPT format
        RecommendationRequest request = new RecommendationRequest();
        request.setFavoritePosts(favoritePosts.stream()
            .map(post -> {
                RecommendationRequest.PostData postData = new RecommendationRequest.PostData();
                postData.setCharacterName(post.getCharacterName());
                postData.setAnime(post.getAnime());
                // use a direct request for genres if they are not loaded
                List<String> genres = post.getAnimeGenre() != null && !post.getAnimeGenre().isEmpty() 
                    ? post.getAnimeGenre() 
                    : characterPostRepository.findGenresByCharacterPostId(post.getId());
                postData.setAnimeGenre(genres.isEmpty() ? List.of() : genres);
                return postData;
            })
            .collect(Collectors.toList()));
        log.debug("Prepared request for ChatGPT: {}", request);

        // get recommendations from ChatGPT
        log.info("Requesting recommendations from ChatGPT");
        RecommendationResponse recommendations = chatGptService.getRecommendations(request);
        log.info("Received recommendations from ChatGPT: {}", recommendations);

        // looking for posts on recommendations
        List<CharacterPost> recommendedPosts = new ArrayList<>();
        
        if (recommendations.getRecommendedAnime() != null && !recommendations.getRecommendedAnime().isEmpty()) {
            log.info("Searching posts by recommended anime: {}", recommendations.getRecommendedAnime());
            List<CharacterPost> animeResults = characterPostRepository.findByAnimeIn(recommendations.getRecommendedAnime());
            log.info("Found {} posts by anime", animeResults.size());
            recommendedPosts.addAll(animeResults);
        }
        
        if (recommendations.getRecommendedGenres() != null && !recommendations.getRecommendedGenres().isEmpty()) {
            log.info("Searching posts by recommended genres: {}", recommendations.getRecommendedGenres());
            List<CharacterPost> genreResults = characterPostRepository.findByAnimeGenreIn(recommendations.getRecommendedGenres());
            log.info("Found {} posts by genres", genreResults.size());
            recommendedPosts.addAll(genreResults);
        }
        
        if (recommendations.getRecommendedCharacters() != null && !recommendations.getRecommendedCharacters().isEmpty()) {
            log.info("Searching posts by recommended characters: {}", recommendations.getRecommendedCharacters());
            List<CharacterPost> characterResults = characterPostRepository.findByCharacterNameIn(recommendations.getRecommendedCharacters());
            log.info("Found {} posts by characters", characterResults.size());
            recommendedPosts.addAll(characterResults);
        }

        // remove duplicates and posts of the current user
        List<CharacterPost> filteredPosts = recommendedPosts.stream()
            .distinct()
            .filter(post -> !post.getUser().getId().equals(userId))
            .collect(Collectors.toList());
            
        log.info("Final recommendations count (after filtering): {}", filteredPosts.size());
        return filteredPosts;
    }
} 