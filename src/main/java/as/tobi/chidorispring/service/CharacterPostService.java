package as.tobi.chidorispring.service;

import as.tobi.chidorispring.dto.characterPost.CharacterPostDTO;
import as.tobi.chidorispring.dto.characterPost.UpdateCharacterPostDTO;
import as.tobi.chidorispring.exceptions.InternalViolationException;
import as.tobi.chidorispring.exceptions.InternalViolationType;
import as.tobi.chidorispring.mapper.CharacterPostMapper;
import as.tobi.chidorispring.entity.CharacterPost;
import as.tobi.chidorispring.entity.UserProfile;
import as.tobi.chidorispring.repository.CharacterPostRepository;
import as.tobi.chidorispring.repository.LikeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CharacterPostService {
    @Autowired
    private CharacterPostRepository postRepository;
    @Autowired
    private LikeRepository likesRepository;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private CharacterPostMapper characterPostMapper;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    @Transactional
    @CacheEvict(value = {"posts", "post"}, allEntries = true)
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
        return characterPostMapper.toDto(savedPost);
    }

    @Cacheable(value = "post", key = "#postId")
    public CharacterPostDTO findPostById(Long postId) {
        log.debug("Finding post by ID: {}", postId);

        CharacterPost post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("Post not found for ID: {}", postId);
                    return new InternalViolationException(InternalViolationType.POST_IS_NOT_EXISTS);
                });

        return characterPostMapper.toDto(post);
    }

    @Cacheable(value = "posts")
    public List<CharacterPostDTO> findAllPosts(int page, int size) {
        log.debug("Finding posts with pagination: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<CharacterPost> posts = postRepository.findAll(pageable);
        return posts.stream()
                .map(characterPostMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = {"posts", "post"}, key = "#postId", allEntries = true)
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
        return characterPostMapper.toDto(updatedPost);
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
}