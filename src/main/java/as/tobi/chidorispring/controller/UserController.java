package as.tobi.chidorispring.controller;

import as.tobi.chidorispring.dto.userProfile.UserProfileDTO;
import as.tobi.chidorispring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        try {
            String email = principal.getName();
            userService.updateUserAvatar(email, file);
            return ResponseEntity.ok("Avatar updated successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload avatar");
        }
    }

    @Transactional(readOnly = true)
    @GetMapping("/avatar")
    public ResponseEntity<byte[]> getAvatar(Principal principal) {
        String email = principal.getName();
        byte[] avatar = userService.getUserAvatar(email);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // или другой подходящий тип
                .body(avatar);
    }

    @GetMapping("/profile")
    @Transactional(readOnly = true)
    public ResponseEntity<UserProfileDTO> getUserProfile(Principal principal) {
        String email = principal.getName();
        UserProfileDTO userDto = userService.getUserProfile(email);
        return ResponseEntity.ok(userDto);
    }

    @Transactional(readOnly = true)
    @GetMapping("/profile-with-avatar")
    public ResponseEntity<UserProfileDTO> getUserProfileWithAvatar(Principal principal) {
        String email = principal.getName();
        UserProfileDTO userDto = userService.getUserProfileWithAvatar(email);
        return ResponseEntity.ok(userDto);
    }

}