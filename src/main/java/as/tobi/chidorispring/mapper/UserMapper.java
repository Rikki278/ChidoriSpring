package as.tobi.chidorispring.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import as.tobi.chidorispring.dto.auth.RegisterRequest;
import as.tobi.chidorispring.dto.characterPost.UserCharacterPostDTO;
import as.tobi.chidorispring.dto.userProfile.UserProfileDTO;
import as.tobi.chidorispring.dto.userProfile.UserProfileShortDTO;
import as.tobi.chidorispring.dto.userProfile.UserProfileWithPostsDTO;
import as.tobi.chidorispring.entity.CharacterPost;
import as.tobi.chidorispring.entity.UserProfile;
import as.tobi.chidorispring.enums.UserRole;
import as.tobi.chidorispring.repository.UserFavoritePostRepository;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserMapper {

    private final PasswordEncoder passwordEncoder;
    private final UserFavoritePostRepository userFavoritePostRepository;

    private final String pfp = "https://res.cloudinary.com/djmpkplp1/image/upload/v1746816860/ChatGPT_Image_9_%D1%82%D1%80%D0%B0%D0%B2._2025_%D1%80._20_41_06_myu8yb.png";

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
                .profileImageUrl(pfp)
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
                .bio(user.getBio())
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
                .bio(user.getBio())
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

    public UserProfileShortDTO toUserProfileShortDto(UserProfile user) {
        return UserProfileShortDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}