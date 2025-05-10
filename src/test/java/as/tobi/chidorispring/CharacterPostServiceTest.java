package as.tobi.chidorispring;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import as.tobi.chidorispring.service.CharacterPostService;
import as.tobi.chidorispring.service.CloudinaryService;
import as.tobi.chidorispring.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import as.tobi.chidorispring.dto.characterPost.CharacterPostDTO;
import as.tobi.chidorispring.dto.characterPost.UpdateCharacterPostDTO;
import as.tobi.chidorispring.dto.userProfile.UserProfileShortDTO;
import as.tobi.chidorispring.entity.CharacterPost;
import as.tobi.chidorispring.entity.UserProfile;
import as.tobi.chidorispring.exceptions.InternalViolationException;
import as.tobi.chidorispring.mapper.CharacterPostMapper;
import as.tobi.chidorispring.repository.CharacterPostRepository;
import as.tobi.chidorispring.repository.UserFavoritePostRepository;
import as.tobi.chidorispring.repository.UserRelationshipRepository;

@ExtendWith(MockitoExtension.class)
class CharacterPostServiceTest {

    @Mock
    private CharacterPostRepository postRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private CharacterPostMapper characterPostMapper;

    @Mock
    private UserService userService;

    @Mock
    private UserFavoritePostRepository favoritePostRepository;

    @Mock
    private UserRelationshipRepository relationshipRepository;

    @InjectMocks
    private CharacterPostService characterPostService;

    private UserProfile testUser;
    private CharacterPost testPost;
    private CharacterPostDTO testPostDTO;
    private MultipartFile testImage;
    private UserProfileShortDTO userProfileShortDTO;
    private UpdateCharacterPostDTO updateCharacterPostDTO;

    @BeforeEach
    void setUp() {
        // Подготовка тестовых данных
        testUser = new UserProfile();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");

        testPost = new CharacterPost();
        testPost.setId(1L);
        testPost.setUser(testUser);
        testPost.setCharacterName("Test Character");
        testPost.setAnime("Test Anime");
        testPost.setDescription("Test Description");
        testPost.setCreatedAt(LocalDateTime.now());

        userProfileShortDTO = UserProfileShortDTO.builder()
            .id(1L)
            .username("testuser")
            .profileImageUrl("http://example.com/avatar.jpg")
            .build();

        testPostDTO = CharacterPostDTO.builder()
            .id(1L)
            .characterName("Test Character")
            .anime("Test Anime")
            .animeGenre(List.of("Action", "Adventure"))
            .description("Test Description")
            .characterImageUrl("http://example.com/character.jpg")
            .author(userProfileShortDTO)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .likeCount(0L)
            .commentCount(0L)
            .isFavorited(false)
            .build();

        updateCharacterPostDTO = new UpdateCharacterPostDTO();
        updateCharacterPostDTO.setCharacterName("Updated Character");

        testImage = new MockMultipartFile(
            "test.jpg",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
    }

    @Test
    void createPost_WithValidData_ShouldCreatePost() {
        // Arrange
        when(cloudinaryService.uploadProfilePicture(any(MultipartFile.class)))
            .thenReturn("http://cloudinary.com/test.jpg");
        when(postRepository.save(any(CharacterPost.class))).thenReturn(testPost);
        when(characterPostMapper.toDto(any(CharacterPost.class), anyLong()))
            .thenReturn(testPostDTO);

        // Act
        CharacterPostDTO result = characterPostService.createPost(testPost, testImage, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testPostDTO.getId(), result.getId());
        verify(cloudinaryService).uploadProfilePicture(testImage);
        verify(postRepository).save(any(CharacterPost.class));
    }

    @Test
    void findPostById_WithValidId_ShouldReturnPost() {
        // Arrange
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userService.getUserByEmail(anyString())).thenReturn(testUser);
        when(characterPostMapper.toDto(any(CharacterPost.class), anyLong()))
            .thenReturn(testPostDTO);

        // Act
        CharacterPostDTO result = characterPostService.findPostById(1L, "test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(testPostDTO.getId(), result.getId());
        verify(postRepository).findById(1L);
    }

    @Test
    void findPostById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InternalViolationException.class, () -> 
            characterPostService.findPostById(999L, "test@example.com")
        );
    }

    @Test
    void updatePost_WithValidData_ShouldUpdatePost() throws Exception {
        // Arrange
        String updateJson = "{\"characterName\":\"Updated Character\"}";
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(characterPostMapper.parseUpdateJson(anyString())).thenReturn(updateCharacterPostDTO);
        when(characterPostMapper.toDto(any(CharacterPost.class), anyLong()))
            .thenReturn(testPostDTO);
        when(postRepository.save(any(CharacterPost.class))).thenReturn(testPost);

        // Act
        CharacterPostDTO result = characterPostService.updatePost(1L, updateJson, null, 1L);

        // Assert
        assertNotNull(result);
        verify(postRepository).save(any(CharacterPost.class));
    }

    @Test
    void updatePost_WithUnauthorizedUser_ShouldThrowException() throws Exception {
        // Arrange
        String updateJson = "{\"characterName\":\"Updated Character\"}";
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(characterPostMapper.parseUpdateJson(anyString())).thenReturn(updateCharacterPostDTO);

        // Act & Assert
        assertThrows(InternalViolationException.class, () -> 
            characterPostService.updatePost(1L, updateJson, null, 999L)
        );
    }

    @Test
    void deletePost_WithValidData_ShouldDeletePost() {
        // Arrange
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        // Act
        characterPostService.deletePost(1L, 1L);

        // Assert
        verify(favoritePostRepository).deleteByCharacterPostId(1L);
        verify(postRepository).delete(testPost);
    }

    @Test
    void getRecommendedPosts_WithFollowingUsers_ShouldReturnPosts() {
        // Arrange
        UserProfile followingUser = new UserProfile();
        followingUser.setId(2L);
        List<UserProfile> followingUsers = Arrays.asList(followingUser);
        
        when(userService.getUserByEmail(anyString())).thenReturn(testUser);
        when(relationshipRepository.findFollowingByUser(any(UserProfile.class)))
            .thenReturn(followingUsers);
        when(postRepository.findByUserIdInOrderByCreatedAtDesc(anyList(), any()))
            .thenReturn(org.springframework.data.domain.Page.empty());

        // Act
        List<CharacterPostDTO> result = characterPostService.getRecommendedPosts("test@example.com", 0, 10);

        // Assert
        assertNotNull(result);
        verify(relationshipRepository).findFollowingByUser(testUser);
    }

    @Test
    void getRecommendedPosts_WithNoFollowingUsers_ShouldReturnEmptyList() {
        // Arrange
        when(userService.getUserByEmail(anyString())).thenReturn(testUser);
        when(relationshipRepository.findFollowingByUser(any(UserProfile.class)))
            .thenReturn(List.of());

        // Act
        List<CharacterPostDTO> result = characterPostService.getRecommendedPosts("test@example.com", 0, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void createPost_WithLargeFile_ShouldThrowException() {
        // Arrange
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MultipartFile largeFile = new MockMultipartFile(
            "large.jpg",
            "large.jpg",
            "image/jpeg",
            largeContent
        );

        // Act & Assert
        assertThrows(InternalViolationException.class, () -> 
            characterPostService.createPost(testPost, largeFile, testUser)
        );
    }
} 