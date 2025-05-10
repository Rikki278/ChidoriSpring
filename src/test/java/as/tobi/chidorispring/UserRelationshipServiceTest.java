package as.tobi.chidorispring;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import as.tobi.chidorispring.service.UserRelationshipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import as.tobi.chidorispring.dto.userProfile.UserProfileShortDTO;
import as.tobi.chidorispring.entity.UserProfile;
import as.tobi.chidorispring.entity.UserRelationship;
import as.tobi.chidorispring.mapper.UserMapper;
import as.tobi.chidorispring.repository.UserProfileRepository;
import as.tobi.chidorispring.repository.UserRelationshipRepository;

@ExtendWith(MockitoExtension.class)
class UserRelationshipServiceTest {

    @Mock
    private UserRelationshipRepository relationshipRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserRelationshipService userRelationshipService;

    private UserProfile testUser;
    private UserProfile followingUser;
    private UserProfileShortDTO userProfileShortDTO;
    private UserRelationship relationship;

    @BeforeEach
    void setUp() {
        // Подготовка тестовых данных
        testUser = new UserProfile();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");

        followingUser = new UserProfile();
        followingUser.setId(2L);
        followingUser.setEmail("following@example.com");
        followingUser.setUsername("followinguser");

        userProfileShortDTO = UserProfileShortDTO.builder()
            .id(2L)
            .username("followinguser")
            .profileImageUrl("http://example.com/avatar.jpg")
            .build();

        relationship = UserRelationship.builder()
            .follower(testUser)
            .following(followingUser)
            .build();
    }

    @Test
    void followUser_WithValidData_ShouldCreateRelationship() {
        // Arrange
        when(userProfileRepository.findById(2L)).thenReturn(Optional.of(followingUser));
        when(relationshipRepository.existsByFollowerAndFollowing(any(UserProfile.class), any(UserProfile.class)))
            .thenReturn(false);

        // Act
        userRelationshipService.followUser(testUser, 2L);

        // Assert
        verify(relationshipRepository).save(any(UserRelationship.class));
    }

    @Test
    void followUser_WithSelfFollow_ShouldThrowException() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            userRelationshipService.followUser(testUser, 1L)
        );
    }

    @Test
    void followUser_WithAlreadyFollowing_ShouldThrowException() {
        // Arrange
        when(userProfileRepository.findById(2L)).thenReturn(Optional.of(followingUser));
        when(relationshipRepository.existsByFollowerAndFollowing(any(UserProfile.class), any(UserProfile.class)))
            .thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            userRelationshipService.followUser(testUser, 2L)
        );
    }

    @Test
    void unfollowUser_WithValidData_ShouldRemoveRelationship() {
        // Arrange
        when(userProfileRepository.findById(2L)).thenReturn(Optional.of(followingUser));

        // Act
        userRelationshipService.unfollowUser(testUser, 2L);

        // Assert
        verify(relationshipRepository).deleteByFollowerAndFollowing(testUser, followingUser);
    }

    @Test
    void getFollowers_WithValidData_ShouldReturnFollowers() {
        // Arrange
        when(userProfileRepository.findById(2L)).thenReturn(Optional.of(followingUser));
        when(relationshipRepository.findFollowersByUser(followingUser))
            .thenReturn(Arrays.asList(testUser));
        when(userMapper.toUserProfileShortDto(testUser))
            .thenReturn(userProfileShortDTO);

        // Act
        List<UserProfileShortDTO> result = userRelationshipService.getFollowers(2L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userProfileShortDTO.getId(), result.get(0).getId());
    }

    @Test
    void getFollowing_WithValidData_ShouldReturnFollowing() {
        // Arrange
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(relationshipRepository.findFollowingByUser(testUser))
            .thenReturn(Arrays.asList(followingUser));
        when(userMapper.toUserProfileShortDto(followingUser))
            .thenReturn(userProfileShortDTO);

        // Act
        List<UserProfileShortDTO> result = userRelationshipService.getFollowing(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userProfileShortDTO.getId(), result.get(0).getId());
    }

    @Test
    void isFollowing_WithFollowingUser_ShouldReturnTrue() {
        // Arrange
        when(userProfileRepository.findById(2L)).thenReturn(Optional.of(followingUser));
        when(relationshipRepository.existsByFollowerAndFollowing(testUser, followingUser))
            .thenReturn(true);

        // Act
        boolean result = userRelationshipService.isFollowing(testUser, 2L);

        // Assert
        assertTrue(result);
    }

    @Test
    void isFollowing_WithNotFollowingUser_ShouldReturnFalse() {
        // Arrange
        when(userProfileRepository.findById(2L)).thenReturn(Optional.of(followingUser));
        when(relationshipRepository.existsByFollowerAndFollowing(testUser, followingUser))
            .thenReturn(false);

        // Act
        boolean result = userRelationshipService.isFollowing(testUser, 2L);

        // Assert
        assertFalse(result);
    }

    @Test
    void getFollowersCount_WithValidData_ShouldReturnCount() {
        // Arrange
        when(userProfileRepository.findById(2L)).thenReturn(Optional.of(followingUser));
        when(relationshipRepository.findFollowersByUser(followingUser))
            .thenReturn(Arrays.asList(testUser));

        // Act
        long result = userRelationshipService.getFollowersCount(2L);

        // Assert
        assertEquals(1, result);
    }

    @Test
    void getFollowingCount_WithValidData_ShouldReturnCount() {
        // Arrange
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(relationshipRepository.findFollowingByUser(testUser))
            .thenReturn(Arrays.asList(followingUser));

        // Act
        long result = userRelationshipService.getFollowingCount(1L);

        // Assert
        assertEquals(1, result);
    }

    @Test
    void getFollowers_WithInvalidUserId_ShouldThrowException() {
        // Arrange
        when(userProfileRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            userRelationshipService.getFollowers(999L)
        );
    }

    @Test
    void getFollowing_WithInvalidUserId_ShouldThrowException() {
        // Arrange
        when(userProfileRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            userRelationshipService.getFollowing(999L)
        );
    }
} 