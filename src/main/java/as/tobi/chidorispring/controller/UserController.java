package as.tobi.chidorispring.controller;

import as.tobi.chidorispring.dto.userProfile.UpdateUserProfileDTO;
import as.tobi.chidorispring.dto.userProfile.UserProfileDTO;
import as.tobi.chidorispring.dto.userProfile.UserProfileWithPostsDTO;
import as.tobi.chidorispring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserProfileDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/profile/picture")
    public ResponseEntity<String> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Principal principal) {

        // get Email with Principal
        String email = principal.getName();

        // call the method from the service to load the avatar
        userService.updateUserAvatar(email, file);

        return ResponseEntity.ok("Avatar uploaded successfully");
    }

    @PatchMapping("/profile/picture")
    public ResponseEntity<String> updateAvatar(
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        String email = principal.getName();
        userService.updateUserAvatar(email, file);
        return ResponseEntity.ok("Avatar updated successfully (PATCH)");
    }

    @GetMapping("/profile")
    @Transactional(readOnly = true)
    public ResponseEntity<UserProfileDTO> getUserProfile(Principal principal) {
        String email = principal.getName();
        UserProfileDTO userDto = userService.getUserProfile(email);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/profile-posts")
    @Transactional(readOnly = true)
    public ResponseEntity<UserProfileWithPostsDTO> getUserProfileWithPosts(Principal principal) {
        String email = principal.getName();
        UserProfileWithPostsDTO userDto = userService.getUserWithPosts(email);
        return ResponseEntity.ok(userDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateUser(
            @RequestBody UpdateUserProfileDTO request,
            Principal principal
    ) {
        String email = principal.getName();
        UserProfileDTO updated = userService.updateUser(email, request);
        return ResponseEntity.ok(updated);
    }



}