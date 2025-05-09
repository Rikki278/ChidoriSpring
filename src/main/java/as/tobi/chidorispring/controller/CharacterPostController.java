package as.tobi.chidorispring.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import as.tobi.chidorispring.dto.characterPost.CharacterPostCommentDTO;
import as.tobi.chidorispring.dto.characterPost.CharacterPostDTO;
import as.tobi.chidorispring.dto.characterPost.CommentRequestDTO;
import as.tobi.chidorispring.entity.CharacterPost;
import as.tobi.chidorispring.exceptions.InternalViolationException;
import as.tobi.chidorispring.exceptions.InternalViolationType;
import as.tobi.chidorispring.service.CharacterPostService;
import as.tobi.chidorispring.service.InteractionService;
import as.tobi.chidorispring.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/posts")
public class CharacterPostController {

    @Autowired
    private CharacterPostService postService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<CharacterPostDTO> createPost(
            @RequestPart("post") CharacterPost post,
            @RequestPart(value = "characterImage", required = false) MultipartFile characterImage,
            Principal principal) {
        CharacterPostDTO createdPost = postService.createPost(post, characterImage, userService.getUserByEmail(principal.getName()));
        return ResponseEntity.ok(createdPost);
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<CharacterPostDTO>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) String anime,
            @RequestParam(required = false) List<String> genres,
            Principal principal) {
        String userEmail = principal != null ? principal.getName() : null;
        List<CharacterPostDTO> posts = postService.findAllPosts(
            page, size, userEmail, sortBy, sortDirection, anime, genres);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{postId}")
    @Transactional(readOnly = true)
    public ResponseEntity<CharacterPostDTO> getPostById(
            @PathVariable Long postId,
            Principal principal) {
        String userEmail = principal != null ? principal.getName() : null;
        CharacterPostDTO post = postService.findPostById(postId, userEmail);
        return ResponseEntity.ok(post);
    }

    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<CharacterPostDTO> updatePost(
            @PathVariable Long postId,
            @RequestPart("updateData") String updateDataJson,
            @RequestPart(value = "newCharacterImage", required = false) MultipartFile newCharacterImage,
            Principal principal) throws JsonProcessingException {
        CharacterPostDTO updatedPost = postService.updatePost(postId, updateDataJson, newCharacterImage, userService.getUserByEmail(principal.getName()).getId());
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{postId}")
    @Transactional
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            Principal principal) {
        Long currentUserId = userService.getUserByEmail(principal.getName()).getId();
        postService.deletePost(postId, currentUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/favorite")
    @Transactional
    public ResponseEntity<Void> addFavoritePost(
            @PathVariable Long postId,
            Principal principal) {
        userService.addFavoritePost(principal.getName(), postId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}/favorite")
    @Transactional
    public ResponseEntity<Void> removeFavoritePost(
            @PathVariable Long postId,
            Principal principal) {
        userService.removeFavoritePost(principal.getName(), postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/favorites")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CharacterPostDTO>> getFavoritePosts(
            Principal principal) {
        if (principal == null) {
            throw new InternalViolationException(InternalViolationType.UNAUTHORIZED_ACCESS);
        }
        List<CharacterPostDTO> favoritePosts = userService.getFavoritePosts(principal.getName());
        return ResponseEntity.ok(favoritePosts);
    }

    @PostMapping("/{postId}/like")
    @Transactional
    public ResponseEntity<Void> addLike(
            @PathVariable Long postId,
            Principal principal) {
        interactionService.addLike(postId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}/like")
    @Transactional
    public ResponseEntity<Void> removeLike(
            @PathVariable Long postId,
            Principal principal) {
        interactionService.removeLike(postId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{postId}/comments", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<CharacterPostCommentDTO> addComment(
            @PathVariable Long postId,
            @RequestBody CommentRequestDTO commentRequest,
            Principal principal) {
        CharacterPostCommentDTO comment = interactionService.addComment(postId, commentRequest.getContent(), principal.getName());
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    @Transactional
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Principal principal) {
        interactionService.deleteComment(commentId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{postId}/comments")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CharacterPostCommentDTO>> getComments(
            @PathVariable Long postId) {
        List<CharacterPostCommentDTO> comments = interactionService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/recommended")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CharacterPostDTO>> getRecommendedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        String userEmail = principal.getName();
        List<CharacterPostDTO> posts = postService.getRecommendedPosts(userEmail, page, size);
        return ResponseEntity.ok(posts);
    }

}