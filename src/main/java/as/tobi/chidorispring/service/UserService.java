package as.tobi.chidorispring.service;

import as.tobi.chidorispring.dto.auth.RegisterRequest;
import as.tobi.chidorispring.dto.characterPost.CharacterPostDTO;
import as.tobi.chidorispring.dto.userProfile.UpdateUserProfileDTO;
import as.tobi.chidorispring.dto.userProfile.UserProfileDTO;
import as.tobi.chidorispring.dto.userProfile.UserProfileWithPostsDTO;
import as.tobi.chidorispring.entity.CharacterPost;
import as.tobi.chidorispring.entity.UserFavoritePost;
import as.tobi.chidorispring.entity.UserProfile;
import as.tobi.chidorispring.enums.UserRole;
import as.tobi.chidorispring.exceptions.InternalViolationException;
import as.tobi.chidorispring.exceptions.InternalViolationType;
import as.tobi.chidorispring.mapper.CharacterPostMapper;
import as.tobi.chidorispring.mapper.UserMapper;
import as.tobi.chidorispring.repository.CharacterPostRepository;
import as.tobi.chidorispring.repository.UserFavoritePostRepository;
import as.tobi.chidorispring.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CharacterPostMapper characterPostMapper;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private CharacterPostRepository postRepository;

    @Autowired
    private UserFavoritePostRepository favoritePostRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    public List<UserProfileDTO> getAllUsers() {
        List<UserProfile> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toUserProfileDto)
                .toList();
    }

    public UserProfile saveUser(RegisterRequest request) {
        log.info("Attempting to register new user with email: {} and username: {}",
                request.getEmail(), request.getUsername());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.error("Registration failed - email already exists: {}", request.getEmail());
            throw new InternalViolationException(InternalViolationType.USER_ALREADY_EXISTS);
        }
        log.debug("Creating user entity from request...");
        UserProfile user = userMapper.toUserEntity(request);
        log.info("User entity created. Before save - Email: {}, Username: {}, FirstName: {}, LastName: {}",
                user.getEmail(), user.getUsername(), user.getFirstName(), user.getLastName());
        UserProfile savedUser = userRepository.save(user);
        log.info("User successfully saved. After save - ID: {}, Email: {}, Username: {}",
                savedUser.getId(), savedUser.getEmail(), savedUser.getUsername());
        return savedUser;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        UserProfile user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InternalViolationException(InternalViolationType.USER_IS_NOT_EXISTS));
        return new User(user.getEmail(), user.getPassword(), List.of());
    }

    public UserProfile loadUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new InternalViolationException(InternalViolationType.USER_IS_NOT_EXISTS));
    }


    public void updateUserAvatar(String email, MultipartFile avatar) {
        UserProfile user = getUserByEmail(email);

        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            cloudinaryService.deleteImage(user.getProfileImageUrl());
        }

        imageSizeCheck(avatar);
        String imageUrl = cloudinaryService.uploadProfilePicture(avatar);
        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);
    }

    public UserProfileDTO getUserProfile(String email) {
        UserProfile user = getUserByEmail(email);
        return userMapper.toUserProfileDto(user);
    }

    public UserProfile getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new InternalViolationException(InternalViolationType.USER_IS_NOT_EXISTS));
    }

    public UserProfileWithPostsDTO getUserWithPosts(String email) {
        UserProfile user = getUserByEmail(email);
        return userMapper.toUserProfileWithPostsDto(user);
    }

    private void imageSizeCheck(MultipartFile file) {
        if (file != null && file.getSize() > MAX_FILE_SIZE) {
            log.warn("File size exceeds limit. Size: {}, Max allowed: {}", file.getSize(), MAX_FILE_SIZE);
            throw new InternalViolationException(InternalViolationType.FILE_TOO_LARGE);
        }
    }

    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new InternalViolationException(InternalViolationType.USER_IS_NOT_EXISTS);
        }
        userRepository.deleteById(id);
    }

    public UserProfileDTO updateUser(String email, UpdateUserProfileDTO request) {
        UserProfile user = getUserByEmail(email);
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getBio() != null) user.setBio(request.getBio());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return userMapper.toUserProfileDto(user);
    }

    public UserProfileWithPostsDTO getUserWithPostsById(Long id) {
        UserProfile user = userRepository.findById(id)
                .orElseThrow(() -> new InternalViolationException(InternalViolationType.USER_IS_NOT_EXISTS));
        return userMapper.toUserProfileWithPostsDto(user);
    }

    @Transactional
    @CacheEvict(value = {"posts", "post"}, allEntries = true)
    public void addFavoritePost(String email, Long postId) {
        log.debug("Adding favorite post ID: {} for user: {}", postId, email);
        UserProfile user = getUserByEmail(email);
        CharacterPost post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("Post not found for ID: {}", postId);
                    return new InternalViolationException(InternalViolationType.POST_IS_NOT_EXISTS);
                });
        if (favoritePostRepository.existsByUserIdAndCharacterPostId(user.getId(), postId)) {
            log.warn("User {} already saved post {}", email, postId);
            throw new InternalViolationException(InternalViolationType.ALREADY_SAVED);
        }
        UserFavoritePost favoritePost = UserFavoritePost.builder()
                .user(user)
                .characterPost(post)
                .createdAt(LocalDateTime.now())
                .build();
        favoritePostRepository.save(favoritePost);
        log.info("Post ID: {} saved as favorite by user: {}", postId, email);
    }

    @Transactional
    @CacheEvict(value = {"posts", "post"}, allEntries = true)
    public void removeFavoritePost(String email, Long postId) {
        log.debug("Removing favorite post ID: {} for user: {}", postId, email);
        UserProfile user = getUserByEmail(email);
        if (!favoritePostRepository.existsByUserIdAndCharacterPostId(user.getId(), postId)) {
            log.warn("User {} has not saved post {}", email, postId);
            throw new InternalViolationException(InternalViolationType.NOT_SAVED);
        }
        favoritePostRepository.deleteByUserIdAndCharacterPostId(user.getId(), postId);
        log.info("Post ID: {} removed from favorites by user: {}", postId, email);
    }

    public List<CharacterPostDTO> getFavoritePosts(String email) {
        log.debug("Retrieving favorite posts for user: {}", email);
        UserProfile user = getUserByEmail(email);
        List<CharacterPost> favoritePosts = postRepository.findFavoritePostsByUserId(user.getId());
        return favoritePosts.stream()
                .map(post -> characterPostMapper.toDto(post, user.getId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateLastLogin(String email) {
        UserProfile user = getUserByEmail(email);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    public List<UserProfileDTO> getUsersByUsername(String username) {
        List<UserProfile> userProfiles = userRepository.findByUsernameContainingIgnoreCase(username);
        if (userProfiles.isEmpty()) {
            new InternalViolationException(InternalViolationType.USER_IS_NOT_EXISTS);
        }
        return userProfiles.stream()
                .map(userMapper::toUserProfileDto)
                .collect(Collectors.toList());
    }
}