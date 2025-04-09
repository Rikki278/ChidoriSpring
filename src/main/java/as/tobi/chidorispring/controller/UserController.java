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

        // get Email with Principal
        String email = principal.getName();

        // call the method from the service to load the avatar
        userService.updateUserAvatar(email, file);

        return ResponseEntity.ok("Avatar uploaded successfully");
    }

    @PatchMapping("/avatar")
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

}