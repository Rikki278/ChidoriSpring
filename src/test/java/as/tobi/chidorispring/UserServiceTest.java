package as.tobi.chidorispring;

import as.tobi.chidorispring.dto.auth.RegisterRequest;
import as.tobi.chidorispring.dto.characterPost.CharacterPostDTO;
import as.tobi.chidorispring.dto.userProfile.UpdateUserProfileDTO;
import as.tobi.chidorispring.dto.userProfile.UserProfileDTO;
import as.tobi.chidorispring.dto.userProfile.UserProfileShortDTO;
import as.tobi.chidorispring.entity.CharacterPost;
import as.tobi.chidorispring.entity.UserFavoritePost;
import as.tobi.chidorispring.entity.UserProfile;
import as.tobi.chidorispring.enums.UserRole;
import as.tobi.chidorispring.exceptions.InternalViolationException;
import as.tobi.chidorispring.mapper.CharacterPostMapper;
import as.tobi.chidorispring.mapper.UserMapper;
import as.tobi.chidorispring.repository.CharacterPostRepository;
import as.tobi.chidorispring.repository.UserFavoritePostRepository;
import as.tobi.chidorispring.repository.UserRepository;
import as.tobi.chidorispring.service.CloudinaryService;
import as.tobi.chidorispring.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CharacterPostMapper characterPostMapper;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private CharacterPostRepository postRepository;

    @Mock
    private UserFavoritePostRepository favoritePostRepository;

    @InjectMocks
    private UserService userService;

    private UserProfile testUser;
    private RegisterRequest registerRequest;
    private UpdateUserProfileDTO updateUserProfileDTO;
    private UserProfileDTO userProfileDTO;
    private CharacterPostDTO characterPostDTO;

    @BeforeEach
    void setUp() {
        testUser = UserProfile.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.ROLE_USER)
                .createdAt(LocalDateTime.now())
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");

        updateUserProfileDTO = new UpdateUserProfileDTO();
        updateUserProfileDTO.setUsername("updateduser");
        updateUserProfileDTO.setEmail("updated@example.com");
        updateUserProfileDTO.setFirstName("Updated");
        updateUserProfileDTO.setLastName("User");
        updateUserProfileDTO.setBio("New bio");

        userProfileDTO = UserProfileDTO.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.ROLE_USER)
                .firstName("Test")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .build();

        UserProfileShortDTO authorDTO = UserProfileShortDTO.builder()
                .id(1L)
                .username("testuser")
                .profileImageUrl("http://example.com/image.jpg")
                .build();

        characterPostDTO = CharacterPostDTO.builder()
                .id(1L)
                .characterName("Test Character")
                .anime("Test Anime")
                .animeGenre(List.of("Action", "Adventure"))
                .description("Test Description")
                .characterImageUrl("http://example.com/character.jpg")
                .author(authorDTO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .likeCount(0)
                .commentCount(0)
                .isFavorited(false)
                .build();
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        List<UserProfile> users = List.of(testUser);
        List<UserProfileDTO> expectedDtos = List.of(userProfileDTO);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toUserProfileDto(any(UserProfile.class))).thenReturn(userProfileDTO);

        List<UserProfileDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(expectedDtos.size(), result.size());
        verify(userRepository).findAll();
        verify(userMapper).toUserProfileDto(any(UserProfile.class));
    }

    @Test
    void saveUser_WithValidData_ShouldSaveUser() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userMapper.toUserEntity(any(RegisterRequest.class))).thenReturn(testUser);
        when(userRepository.save(any(UserProfile.class))).thenReturn(testUser);

        UserProfile result = userService.saveUser(registerRequest);

        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).save(any(UserProfile.class));
    }

    @Test
    void saveUser_WithExistingEmail_ShouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        assertThrows(InternalViolationException.class, () -> userService.saveUser(registerRequest));
    }

    @Test
    void loadUserByUsername_WithValidEmail_ShouldReturnUserDetails() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        UserDetails result = userService.loadUserByUsername("test@example.com");

        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getUsername());
    }

    @Test
    void loadUserByUsername_WithInvalidEmail_ShouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(InternalViolationException.class, () -> userService.loadUserByUsername("invalid@example.com"));
    }

    @Test
    void updateUserAvatar_WithValidFile_ShouldUpdateAvatar() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(cloudinaryService.uploadProfilePicture(any(MultipartFile.class))).thenReturn("new-image-url");

        userService.updateUserAvatar("test@example.com", file);

        verify(cloudinaryService).uploadProfilePicture(any(MultipartFile.class));
        verify(userRepository).save(any(UserProfile.class));
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUser() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userMapper.toUserProfileDto(any(UserProfile.class))).thenReturn(userProfileDTO);

        UserProfileDTO result = userService.updateUser("test@example.com", updateUserProfileDTO);

        assertNotNull(result);
        verify(userRepository).save(any(UserProfile.class));
    }

    @Test
    void addFavoritePost_WithValidData_ShouldAddFavorite() {
        CharacterPost post = new CharacterPost();
        post.setId(1L);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));
        when(favoritePostRepository.existsByUserIdAndCharacterPostId(anyLong(), anyLong())).thenReturn(false);

        userService.addFavoritePost("test@example.com", 1L);

        verify(favoritePostRepository).save(any(UserFavoritePost.class));
    }

    @Test
    void removeFavoritePost_WithValidData_ShouldRemoveFavorite() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(favoritePostRepository.existsByUserIdAndCharacterPostId(anyLong(), anyLong())).thenReturn(true);

        userService.removeFavoritePost("test@example.com", 1L);

        verify(favoritePostRepository).deleteByUserIdAndCharacterPostId(anyLong(), anyLong());
    }

    @Test
    void getFavoritePosts_ShouldReturnListOfPosts() {
        List<CharacterPost> posts = List.of(new CharacterPost());
        List<CharacterPostDTO> expectedDtos = List.of(characterPostDTO);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(postRepository.findFavoritePostsByUserId(anyLong())).thenReturn(posts);
        when(characterPostMapper.toDto(any(CharacterPost.class), anyLong())).thenReturn(characterPostDTO);

        List<CharacterPostDTO> result = userService.getFavoritePosts("test@example.com");

        assertNotNull(result);
        assertEquals(expectedDtos.size(), result.size());
        verify(postRepository).findFavoritePostsByUserId(anyLong());
    }

    @Test
    void updateLastLogin_ShouldUpdateLastLoginTime() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        userService.updateLastLogin("test@example.com");

        verify(userRepository).save(any(UserProfile.class));
    }
} 