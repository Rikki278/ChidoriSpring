package as.tobi.chidorispring.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import as.tobi.chidorispring.dto.characterPost.CharacterPostDTO;
import as.tobi.chidorispring.dto.characterPost.UpdateCharacterPostDTO;
import as.tobi.chidorispring.entity.CharacterPost;
import as.tobi.chidorispring.entity.UserProfile;
import as.tobi.chidorispring.exceptions.InternalViolationException;
import as.tobi.chidorispring.exceptions.InternalViolationType;
import as.tobi.chidorispring.mapper.CharacterPostMapper;
import as.tobi.chidorispring.repository.CharacterPostRepository;
import as.tobi.chidorispring.repository.UserFavoritePostRepository;
import as.tobi.chidorispring.repository.UserRelationshipRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CharacterPostService {
    @Autowired
    private CharacterPostRepository postRepository;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private CharacterPostMapper characterPostMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private UserFavoritePostRepository favoritePostRepository;
    @Autowired
    private UserRelationshipRepository relationshipRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    @Transactional
    public CharacterPostDTO createPost(CharacterPost post,
                                       MultipartFile characterImage,
                                       UserProfile user) {
        log.debug("Creating new post for user: {}", user.getId());
        post.setUser(user);

        if (characterImage != null && !characterImage.isEmpty()) {
            log.debug("Uploading character image for post");
            String imageUrl = cloudinaryService.uploadProfilePicture(characterImage);

            post.setCharacterImageUrl(imageUrl);
            log.info("Character image uploaded successfully. URL: {}", imageUrl);
        }

        imageSizeCheck(characterImage);
        CharacterPost savedPost = postRepository.save(post);

        log.info("Post created successfully. Post ID: {}", savedPost.getId());
        return characterPostMapper.toDto(savedPost, user.getId());
    }

    public CharacterPostDTO findPostById(Long postId, String userEmail) {
        log.debug("Finding post by ID: {}", postId);
        CharacterPost post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("Post not found for ID: {}", postId);
                    return new InternalViolationException(InternalViolationType.POST_IS_NOT_EXISTS);
                });
        Long currentUserId = userEmail != null ? userService.getUserByEmail(userEmail).getId() : null;
        return characterPostMapper.toDto(post, currentUserId);
    }

    @Cacheable(value = "posts", key = "{#page, #size, #userEmail, #sortBy, #sortDirection, #anime, #genres}")
    public List<CharacterPostDTO> findAllPosts(
            int page, 
            int size, 
            String userEmail,
            String sortBy,
            String sortDirection,
            String anime,
            List<String> genres) {
        log.debug("Finding posts with pagination: page={}, size={}, sortBy={}, sortDirection={}, anime={}, genres={}", 
                page, size, sortBy, sortDirection, anime, genres);
        
        // Create sort object
        Sort sort = Sort.by(
            sortDirection != null && sortDirection.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC,
            sortBy != null ? sortBy : "createdAt"
        );
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get posts based on filters
        Page<CharacterPost> posts;
        if (anime != null && !anime.isEmpty()) {
            posts = postRepository.findByAnimeContainingIgnoreCase(anime, pageable);
        } else if (genres != null && !genres.isEmpty()) {
            // TODO: Implement genre filtering
            posts = postRepository.findAllWithLikesAndComments(pageable);
        } else {
            posts = postRepository.findAllWithLikesAndComments(pageable);
        }
        
        Long currentUserId = userEmail != null ? userService.getUserByEmail(userEmail).getId() : null;

        return posts.stream()
                .map(post -> characterPostMapper.toDto(post, currentUserId))
                .collect(Collectors.toList());
    }

    @Transactional
    public CharacterPostDTO updatePost(Long postId,
                                       String updateDataJson,
                                       MultipartFile newCharacterImage,
                                       Long currentUserId) throws JsonProcessingException {
        log.debug("Updating post ID: {} by user ID: {}", postId, currentUserId);
        UpdateCharacterPostDTO updateDTO = characterPostMapper.parseUpdateJson(updateDataJson);
        updateDTO.setNewCharacterImage(newCharacterImage);
        CharacterPost post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("Post not found for ID: {}", postId);
                    return new InternalViolationException(InternalViolationType.POST_IS_NOT_EXISTS);
                });

        if (!post.getUser().getId().equals(currentUserId)) {
            log.warn("Unauthorized update attempt. User ID: {} tried to update post ID: {} owned by user ID: {}",
                    currentUserId, postId, post.getUser().getId());
            throw new InternalViolationException(InternalViolationType.UNAUTHORIZED_ACCESS);
        }

        imageSizeCheck(newCharacterImage);
        characterPostMapper.updateEntityFromDto(updateDTO, post);

        managePostImage(post, updateDTO);
        post.setUpdatedAt(LocalDateTime.now());

        CharacterPost updatedPost = postRepository.save(post);
        log.info("Post ID: {} updated successfully", postId);

        return characterPostMapper.toDto(updatedPost, currentUserId);
    }

    @Transactional
    public void deletePost(Long postId, Long currentUserId) {
        log.debug("Deleting post ID: {} by user ID: {}", postId, currentUserId);
        CharacterPost post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("Post not found for ID: {}", postId);
                    return new InternalViolationException(InternalViolationType.POST_IS_NOT_EXISTS);
                });

        // check that the user is the author of the post
        if (!post.getUser().getId().equals(currentUserId)) {
            log.warn("Unauthorized delete attempt. User ID: {} tried to delete post ID: {} owned by user ID: {}",
                    currentUserId, postId, post.getUser().getId());
            throw new InternalViolationException(InternalViolationType.UNAUTHORIZED_ACCESS);
        }

        // remove the image from Cloudinary, if it is
        if (post.getCharacterImageUrl() != null) {
            log.debug("Removing character image for post ID: {} from Cloudinary", postId);
            cloudinaryService.deleteImage(post.getCharacterImageUrl());
            log.info("Character image removed for post ID: {} from Cloudinary", postId);
        }

        // delete entries in the chosen
        favoritePostRepository.deleteByCharacterPostId(postId);
        log.debug("Favorite records for post ID: {} removed", postId);

        // Likes and comments will be deleted automatically thanks to cascade = CASCADETYPE.ALL and Orphanremoval = True
        // essentially Characterpost (connections with Likes and Comments)

        postRepository.delete(post);
        log.info("Post ID: {} deleted successfully by user ID: {}", postId, currentUserId);
    }

    private void managePostImage(CharacterPost post, UpdateCharacterPostDTO updateDTO) {

        if (Boolean.TRUE.equals(updateDTO.getShouldRemoveImage())) {

            if (post.getCharacterImageUrl() != null) {
                log.debug("Removing character image for post ID: {}", post.getId());
                cloudinaryService.deleteImage(post.getCharacterImageUrl());

                post.setCharacterImageUrl(null);
                log.info("Character image removed for post ID: {}", post.getId());
            }

        } else if (updateDTO.getNewCharacterImage() != null
                && !updateDTO.getNewCharacterImage().isEmpty()) {

            log.debug("Uploading new character image for post ID: {}", post.getId());
            String newImageUrl = cloudinaryService.uploadProfilePicture(updateDTO.getNewCharacterImage());

            if (post.getCharacterImageUrl() != null) {
                log.debug("Deleting old character image for post ID: {}", post.getId());
                cloudinaryService.deleteImage(post.getCharacterImageUrl());
            }

            post.setCharacterImageUrl(newImageUrl);
            log.info("New character image uploaded for post ID: {}. URL: {}", post.getId(), newImageUrl);
        }
    }

    private void imageSizeCheck(MultipartFile file) {
        if (file != null && file.getSize() > MAX_FILE_SIZE) {
            log.warn("File size exceeds limit. Size: {}, Max allowed: {}", file.getSize(), MAX_FILE_SIZE);
            throw new InternalViolationException(InternalViolationType.FILE_TOO_LARGE);
        }
    }

    // @Cacheable(value = "recommended_posts", key = "{#userEmail, #page, #size}")
    public List<CharacterPostDTO> getRecommendedPosts(String userEmail, int page, int size) {
        log.debug("Getting recommended posts for user: {}", userEmail);
        
        UserProfile currentUser = userService.getUserByEmail(userEmail);
        Pageable pageable = PageRequest.of(page, size);

        // Get posts from users that the current user follows
        List<UserProfile> followingUsers = relationshipRepository.findFollowingByUser(currentUser);
        List<Long> followingUserIds = followingUsers.stream()
                .map(UserProfile::getId)
                .collect(Collectors.toList());
        
        log.debug("Following users count: {}", followingUserIds.size());

        // If user has no following, return empty list
        if (followingUserIds.isEmpty()) {
            log.debug("User has no following, returning empty list");
            return new ArrayList<>();
        }

        // Get posts from followed users
        Page<CharacterPost> followedUsersPosts = postRepository.findByUserIdInOrderByCreatedAtDesc(
                followingUserIds, pageable);
        
        log.debug("Found {} posts from followed users", followedUsersPosts.getContent().size());

        return followedUsersPosts.getContent().stream()
                .map(post -> characterPostMapper.toDto(post, currentUser.getId()))
                .collect(Collectors.toList());
    }

}