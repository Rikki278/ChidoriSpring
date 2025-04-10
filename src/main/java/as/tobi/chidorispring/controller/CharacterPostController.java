package as.tobi.chidorispring.controller;

import as.tobi.chidorispring.dto.characterPost.CharacterPostDTO;
import as.tobi.chidorispring.dto.characterPost.UpdateCharacterPostDTO;
import as.tobi.chidorispring.entity.CharacterPost;
import as.tobi.chidorispring.entity.UserProfile;
import as.tobi.chidorispring.service.CharacterPostService;
import as.tobi.chidorispring.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CharacterPostController {

    @Autowired
    private CharacterPostService postService;

    @Autowired
    private UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CharacterPostDTO> createPost(
            @RequestPart("post") CharacterPost post,
            @RequestPart(value = "characterImage", required = false) MultipartFile characterImage,
            Principal principal) {

        UserProfile user = userService.getUserByEmail(principal.getName());
        CharacterPostDTO result = postService.createPost(post, characterImage, user);

        return ResponseEntity.ok(result);
    }

    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CharacterPostDTO> updatePost(
            @PathVariable Long postId,
            @RequestPart(value = "updateData", required = false) String updateDataJson,
            @RequestPart(value = "newCharacterImage", required = false) MultipartFile newCharacterImage,
            Principal principal) throws JsonProcessingException {

        UserProfile currentUser = userService.getUserByEmail(principal.getName());
        CharacterPostDTO updatedPost = postService.updatePost(
                postId,
                updateDataJson,
                newCharacterImage,
                currentUser.getId()
        );

        return ResponseEntity.ok(updatedPost);
    }

}