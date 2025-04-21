package as.tobi.chidorispring.mapper;

import as.tobi.chidorispring.dto.auth.RegisterRequest;
import as.tobi.chidorispring.dto.characterPost.UserCharacterPostDTO;
import as.tobi.chidorispring.dto.userProfile.UserProfileDTO;
import as.tobi.chidorispring.dto.userProfile.UserProfileWithPostsDTO;
import as.tobi.chidorispring.entity.CharacterPost;
import as.tobi.chidorispring.entity.UserFavoritePost;
import as.tobi.chidorispring.entity.UserProfile;
import as.tobi.chidorispring.enums.UserRole;
import as.tobi.chidorispring.repository.UserFavoritePostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class UserMapper {

    private final PasswordEncoder passwordEncoder;
    private final UserFavoritePostRepository userFavoritePostRepository;

    @Autowired
    public UserMapper(PasswordEncoder passwordEncoder, UserFavoritePostRepository userFavoritePostRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userFavoritePostRepository = userFavoritePostRepository;
    }

    public UserProfile toUserEntity(RegisterRequest request) {
        UserProfile userProfile = UserProfile.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(UserRole.ROLE_USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return userProfile;
    }

    public UserProfileDTO toUserProfileDto(UserProfile user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profileImageUrl(user.getProfileImageUrl())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public UserProfileWithPostsDTO toUserProfileWithPostsDto(UserProfile user) {
        List<UserCharacterPostDTO> postDtos = user.getCharacterPosts().stream()
                .map(post -> toCharacterPostDto(post, user.getId()))
                .toList();

        List<UserCharacterPostDTO> favoritePostDtos = user.getFavoritePosts().stream()
                .map(favoritePost -> toCharacterPostDto(favoritePost.getCharacterPost(), user.getId()))
                .toList();

        return UserProfileWithPostsDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profileImageUrl(user.getProfileImageUrl())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .characterPosts(postDtos)
                .favoritePosts(favoritePostDtos)
                .build();
    }

    private UserCharacterPostDTO toCharacterPostDto(CharacterPost post, Long currentUserId) {
        boolean isFavorited = userFavoritePostRepository.existsByUserIdAndCharacterPostId(currentUserId, post.getId());

        return UserCharacterPostDTO.builder()
                .id(post.getId())
                .characterName(post.getCharacterName())
                .anime(post.getAnime())
                .animeGenre(post.getAnimeGenre())
                .description(post.getDescription())
                .characterImageUrl(post.getCharacterImageUrl())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .likeCount(post.getLikes().size()) // Calculate like count
                .commentCount(post.getComments().size()) // Calculate comment count
                .isFavorited(isFavorited)
                .build();
    }
}