//package as.tobi.chidorispring;
//
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//import as.tobi.chidorispring.service.InteractionService;
//import as.tobi.chidorispring.service.UserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.ArgumentMatchers.anyString;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import as.tobi.chidorispring.dto.characterPost.CharacterPostCommentDTO;
//import as.tobi.chidorispring.dto.userProfile.UserProfileShortDTO;
//import as.tobi.chidorispring.entity.CharacterPost;
//import as.tobi.chidorispring.entity.CharacterPostComment;
//import as.tobi.chidorispring.entity.CharacterPostLike;
//import as.tobi.chidorispring.entity.UserProfile;
//import as.tobi.chidorispring.exceptions.InternalViolationException;
//import as.tobi.chidorispring.mapper.CharacterPostMapper;
//import as.tobi.chidorispring.repository.CharacterPostRepository;
//import as.tobi.chidorispring.repository.CommentRepository;
//import as.tobi.chidorispring.repository.LikeRepository;
//
//@ExtendWith(MockitoExtension.class)
//class InteractionServiceTest {
//
//    @Mock
//    private CharacterPostRepository postRepository;
//
//    @Mock
//    private LikeRepository likeRepository;
//
//    @Mock
//    private CommentRepository commentRepository;
//
//    @Mock
//    private UserService userService;
//
//    @Mock
//    private CharacterPostMapper characterPostMapper;
//
//    @InjectMocks
//    private InteractionService interactionService;
//
//    private UserProfile testUser;
//    private CharacterPost testPost;
//    private CharacterPostComment testComment;
//    private CharacterPostCommentDTO testCommentDTO;
//    private UserProfileShortDTO userProfileShortDTO;
//
//    @BeforeEach
//    void setUp() {
//        // Подготовка тестовых данных
//        testUser = new UserProfile();
//        testUser.setId(1L);
//        testUser.setEmail("test@example.com");
//        testUser.setUsername("testuser");
//
//        testPost = new CharacterPost();
//        testPost.setId(1L);
//        testPost.setUser(testUser);
//        testPost.setCharacterName("Test Character");
//        testPost.setAnime("Test Anime");
//        testPost.setDescription("Test Description");
//        testPost.setCreatedAt(LocalDateTime.now());
//
//        userProfileShortDTO = UserProfileShortDTO.builder()
//            .id(1L)
//            .username("testuser")
//            .profileImageUrl("http://example.com/avatar.jpg")
//            .build();
//
//        testComment = CharacterPostComment.builder()
//            .id(1L)
//            .content("Test comment")
//            .characterPost(testPost)
//            .user(testUser)
//            .createdAt(LocalDateTime.now())
//            .updatedAt(LocalDateTime.now())
//            .build();
//
//        testCommentDTO = CharacterPostCommentDTO.builder()
//            .id(1L)
//            .content("Test comment")
//            .author(userProfileShortDTO)
//            .createdAt(LocalDateTime.now())
//            .updatedAt(LocalDateTime.now())
//            .build();
//    }
//
//    @Test
//    void addLike_WithValidData_ShouldAddLike() {
//        // Arrange
//        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
//        when(userService.getUserByEmail(anyString())).thenReturn(testUser);
//        when(likeRepository.existsByCharacterPostIdAndUserId(anyLong(), anyLong())).thenReturn(false);
//
//        // Act
//        interactionService.addLike(1L, "test@example.com");
//
//        // Assert
//        verify(likeRepository).save(any(CharacterPostLike.class));
//    }
//
//    @Test
//    void addLike_WithAlreadyLikedPost_ShouldThrowException() {
//        // Arrange
//        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
//        when(userService.getUserByEmail(anyString())).thenReturn(testUser);
//        when(likeRepository.existsByCharacterPostIdAndUserId(anyLong(), anyLong())).thenReturn(true);
//
//        // Act & Assert
//        assertThrows(InternalViolationException.class, () ->
//            interactionService.addLike(1L, "test@example.com")
//        );
//    }
//
//    @Test
//    void removeLike_WithValidData_ShouldRemoveLike() {
//        // Arrange
//        when(userService.getUserByEmail(anyString())).thenReturn(testUser);
//        when(likeRepository.existsByCharacterPostIdAndUserId(anyLong(), anyLong())).thenReturn(true);
//
//        // Act
//        interactionService.removeLike(1L, "test@example.com");
//
//        // Assert
//        verify(likeRepository).deleteByCharacterPostIdAndUserId(1L, 1L);
//    }
//
//    @Test
//    void removeLike_WithNotLikedPost_ShouldThrowException() {
//        // Arrange
//        when(userService.getUserByEmail(anyString())).thenReturn(testUser);
//        when(likeRepository.existsByCharacterPostIdAndUserId(anyLong(), anyLong())).thenReturn(false);
//
//        // Act & Assert
//        assertThrows(InternalViolationException.class, () ->
//            interactionService.removeLike(1L, "test@example.com")
//        );
//    }
//
//    @Test
//    void addComment_WithValidData_ShouldAddComment() {
//        // Arrange
//        String commentContent = "Test comment";
//        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
//        when(userService.getUserByEmail(anyString())).thenReturn(testUser);
//        when(commentRepository.save(any(CharacterPostComment.class))).thenReturn(testComment);
//        when(characterPostMapper.toCommentDto(any(CharacterPostComment.class))).thenReturn(testCommentDTO);
//
//        // Act
//        CharacterPostCommentDTO result = interactionService.addComment(1L, commentContent, "test@example.com");
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(testCommentDTO.getId(), result.getId());
//        assertEquals(testCommentDTO.getContent(), result.getContent());
//        verify(commentRepository).save(any(CharacterPostComment.class));
//    }
//
//    @Test
//    void deleteComment_WithValidData_ShouldDeleteComment() {
//        // Arrange
//        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
//        when(userService.getUserByEmail(anyString())).thenReturn(testUser);
//
//        // Act
//        interactionService.deleteComment(1L, "test@example.com");
//
//        // Assert
//        verify(commentRepository).delete(testComment);
//    }
//
//    @Test
//    void deleteComment_WithUnauthorizedUser_ShouldThrowException() {
//        // Arrange
//        UserProfile otherUser = new UserProfile();
//        otherUser.setId(2L);
//        otherUser.setEmail("other@example.com");
//
//        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
//        when(userService.getUserByEmail(anyString())).thenReturn(otherUser);
//
//        // Act & Assert
//        assertThrows(InternalViolationException.class, () ->
//            interactionService.deleteComment(1L, "other@example.com")
//        );
//    }
//
//    @Test
//    void getCommentsByPostId_WithValidData_ShouldReturnComments() {
//        // Arrange
//        testPost.setComments(Arrays.asList(testComment));
//        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
//        when(characterPostMapper.toCommentDto(any(CharacterPostComment.class))).thenReturn(testCommentDTO);
//
//        // Act
//        List<CharacterPostCommentDTO> result = interactionService.getCommentsByPostId(1L);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals(testCommentDTO.getId(), result.get(0).getId());
//    }
//
//    @Test
//    void getCommentsByPostId_WithInvalidPostId_ShouldThrowException() {
//        // Arrange
//        when(postRepository.findById(999L)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(InternalViolationException.class, () ->
//            interactionService.getCommentsByPostId(999L)
//        );
//    }
//}