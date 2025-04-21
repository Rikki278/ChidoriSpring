package as.tobi.chidorispring.service;

import as.tobi.chidorispring.dto.characterPost.CharacterPostCommentDTO;
import as.tobi.chidorispring.entity.CharacterPost;
import as.tobi.chidorispring.entity.CharacterPostComment;
import as.tobi.chidorispring.entity.CharacterPostLike;
import as.tobi.chidorispring.entity.UserProfile;
import as.tobi.chidorispring.exceptions.InternalViolationException;
import as.tobi.chidorispring.exceptions.InternalViolationType;
import as.tobi.chidorispring.mapper.CharacterPostMapper;
import as.tobi.chidorispring.repository.CommentRepository;
import as.tobi.chidorispring.repository.LikeRepository;
import as.tobi.chidorispring.repository.CharacterPostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InteractionService {

    @Autowired
    private CharacterPostRepository postRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CharacterPostMapper characterPostMapper;

    @Transactional
    @CacheEvict(value = {"posts", "post"}, allEntries = true)
    public void addLike(Long postId, String userEmail) {
        log.debug("Adding like to post ID: {} by user: {}", postId, userEmail);
        CharacterPost post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("Post not found for ID: {}", postId);
                    return new InternalViolationException(InternalViolationType.POST_IS_NOT_EXISTS);
                });

        UserProfile user = userService.getUserByEmail(userEmail);

        if (likeRepository.existsByCharacterPostIdAndUserId(postId, user.getId())) {
            log.warn("User {} already liked post {}", userEmail, postId);
            throw new InternalViolationException(InternalViolationType.ALREADY_LIKED);
        }

        CharacterPostLike like = CharacterPostLike.builder()
                .characterPost(post)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        likeRepository.save(like);
        log.info("Like added to post ID: {} by user: {}", postId, userEmail);
    }

    @Transactional
    @CacheEvict(value = {"posts", "post"}, allEntries = true)
    public void removeLike(Long postId, String userEmail) {
        log.debug("Removing like from post ID: {} by user: {}", postId, userEmail);
        UserProfile user = userService.getUserByEmail(userEmail);

        if (!likeRepository.existsByCharacterPostIdAndUserId(postId, user.getId())) {
            log.warn("User {} has not liked post {}", userEmail, postId);
            throw new InternalViolationException(InternalViolationType.NOT_LIKED);
        }

        likeRepository.deleteByCharacterPostIdAndUserId(postId, user.getId());
        log.info("Like removed from post ID: {} by user: {}", postId, userEmail);
    }

    @Transactional
    @CacheEvict(value = {"posts", "post"}, allEntries = true)
    public CharacterPostCommentDTO addComment(Long postId, String content, String userEmail) {
        log.debug("Adding comment to post ID: {} by user: {}", postId, userEmail);
        CharacterPost post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("Post not found for ID: {}", postId);
                    return new InternalViolationException(InternalViolationType.POST_IS_NOT_EXISTS);
                });

        UserProfile user = userService.getUserByEmail(userEmail);
        CharacterPostComment comment = CharacterPostComment.builder()
                .content(content)
                .characterPost(post)
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CharacterPostComment savedComment = commentRepository.save(comment);
        log.info("Comment added to post ID: {} by user: {}", postId, userEmail);

        return characterPostMapper.toCommentDto(savedComment);
    }

    @Transactional
    public void deleteComment(Long commentId, String userEmail) {
        log.debug("Deleting comment ID: {} by user: {}", commentId, userEmail);
        CharacterPostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("Comment not found for ID: {}", commentId);
                    return new InternalViolationException(InternalViolationType.COMMENT_NOT_FOUND);
                });

        UserProfile user = userService.getUserByEmail(userEmail);

        if (!comment.getUser().getId().equals(user.getId())) {
            log.warn("Unauthorized attempt to delete comment ID: {} by user: {}", commentId, userEmail);
            throw new InternalViolationException(InternalViolationType.UNAUTHORIZED_ACCESS);
        }

        commentRepository.delete(comment);
        log.info("Comment ID: {} deleted by user: {}", commentId, userEmail);
    }

    public List<CharacterPostCommentDTO> getCommentsByPostId(Long postId) {
        log.debug("Retrieving comments for post ID: {}", postId);

        CharacterPost post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("Post not found for ID: {}", postId);
                    return new InternalViolationException(InternalViolationType.POST_IS_NOT_EXISTS);
                });

        return post.getComments().stream()
                .map(characterPostMapper::toCommentDto)
                .collect(Collectors.toList());
    }
}