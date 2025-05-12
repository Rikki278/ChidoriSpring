package as.tobi.chidorispring.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import as.tobi.chidorispring.dto.characterPost.CharacterPostDTO;
import as.tobi.chidorispring.dto.userProfile.UserProfileShortDTO;
import as.tobi.chidorispring.entity.CharacterPost;
import as.tobi.chidorispring.service.RecommendationService;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CharacterPostDTO>> getRecommendationsForUser(@PathVariable Long userId) {
        List<CharacterPost> recommendations = recommendationService.getRecommendationsForUser(userId);
        List<CharacterPostDTO> dtos = recommendations.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private CharacterPostDTO convertToDto(CharacterPost post) {
        // Diagnostic logging
        System.out.println("Processing post ID: " + post.getId());
        System.out.println("Post character name: " + post.getCharacterName());
        System.out.println("Post anime: " + post.getAnime());
        
        // Ensure genres are never null
        List<String> genres = post.getAnimeGenre() != null ? post.getAnimeGenre() : new ArrayList<>();
        System.out.println("Genres for post " + post.getId() + ": " + genres);

        return CharacterPostDTO.builder()
            .id(post.getId())
            .characterName(post.getCharacterName())
            .anime(post.getAnime())
            .animeGenre(genres)
            .description(post.getDescription())
            .characterImageUrl(post.getCharacterImageUrl())
            .author(post.getUser() != null ? 
                UserProfileShortDTO.builder()
                    .id(post.getUser().getId())
                    .username(post.getUser().getUsername())
                    .profileImageUrl(post.getUser().getProfileImageUrl())
                    .build() 
                : null)
            .createdAt(post.getCreatedAt())
            .updatedAt(post.getUpdatedAt())
            .likeCount(post.getLikes().size())
            .commentCount(post.getComments().size())
            .isLiked(false)
            .isFavorited(false)
            .build();
    }
} 