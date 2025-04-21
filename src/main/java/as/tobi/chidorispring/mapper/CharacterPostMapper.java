package as.tobi.chidorispring.mapper;

import as.tobi.chidorispring.dto.characterPost.CharacterPostCommentDTO;
import as.tobi.chidorispring.dto.characterPost.CharacterPostDTO;
import as.tobi.chidorispring.dto.characterPost.UpdateCharacterPostDTO;
import as.tobi.chidorispring.dto.userProfile.UserProfileShortDTO;
import as.tobi.chidorispring.entity.CharacterPost;
import as.tobi.chidorispring.entity.CharacterPostComment;
import as.tobi.chidorispring.entity.UserProfile;
import as.tobi.chidorispring.repository.UserFavoritePostRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CharacterPostMapper {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserFavoritePostRepository userFavoritePostRepository;

    public CharacterPostDTO toDto(CharacterPost post, Long currentUserId) {
        boolean isFavorited = currentUserId != null && userFavoritePostRepository.existsByUserIdAndCharacterPostId(currentUserId, post.getId());

        return CharacterPostDTO.builder()
                .id(post.getId())
                .characterName(post.getCharacterName())
                .anime(post.getAnime())
                .animeGenre(post.getAnimeGenre())
                .description(post.getDescription())
                .characterImageUrl(post.getCharacterImageUrl())
                .author(toUserShortDto(post.getUser()))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .likeCount(post.getLikes().size()) // Calculate like count
                .commentCount(post.getComments().size()) // Calculate comment count
                .isFavorited(isFavorited)
                .build();
    }

    // Overload for cases where currentUserId is not provided
    public CharacterPostDTO toDto(CharacterPost post) {
        return toDto(post, null);
    }

    public CharacterPostCommentDTO toCommentDto(CharacterPostComment comment) {
        return CharacterPostCommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(toUserShortDto(comment.getUser()))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    private UserProfileShortDTO toUserShortDto(UserProfile user) {
        return UserProfileShortDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    public void updateEntityFromDto(UpdateCharacterPostDTO dto, CharacterPost entity) {
        if (dto.getCharacterName() != null) {
            entity.setCharacterName(dto.getCharacterName());
        }
        if (dto.getAnime() != null) {
            entity.setAnime(dto.getAnime());
        }
        if (dto.getAnimeGenre() != null) {
            entity.setAnimeGenre(dto.getAnimeGenre());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
    }

    public UpdateCharacterPostDTO parseUpdateJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, UpdateCharacterPostDTO.class);
    }
}