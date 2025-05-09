package as.tobi.chidorispring.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import as.tobi.chidorispring.dto.userProfile.UserProfileShortDTO;
import as.tobi.chidorispring.entity.UserProfile;
import as.tobi.chidorispring.service.UserRelationshipService;
import as.tobi.chidorispring.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/relationships")
@RequiredArgsConstructor
public class UserRelationshipController {
    private final UserRelationshipService relationshipService;
    private final UserService userService;

    @PostMapping("/{userId}/follow")
    public ResponseEntity<Void> followUser(
            Principal principal,
            @PathVariable Long userId) {
        UserProfile currentUser = userService.getUserByEmail(principal.getName());
        relationshipService.followUser(currentUser, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/unfollow")
    public ResponseEntity<Void> unfollowUser(
            Principal principal,
            @PathVariable Long userId) {
        UserProfile currentUser = userService.getUserByEmail(principal.getName());
        relationshipService.unfollowUser(currentUser, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserProfileShortDTO>> getFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(relationshipService.getFollowers(userId));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<List<UserProfileShortDTO>> getFollowing(@PathVariable Long userId) {
        return ResponseEntity.ok(relationshipService.getFollowing(userId));
    }

    @GetMapping("/{userId}/is-following")
    public ResponseEntity<Boolean> isFollowing(
            Principal principal,
            @PathVariable Long userId) {
        UserProfile currentUser = userService.getUserByEmail(principal.getName());
        return ResponseEntity.ok(relationshipService.isFollowing(currentUser, userId));
    }

    @GetMapping("/{userId}/followers/count")
    public ResponseEntity<Long> getFollowersCount(@PathVariable Long userId) {
        return ResponseEntity.ok(relationshipService.getFollowersCount(userId));
    }

    @GetMapping("/{userId}/following/count")
    public ResponseEntity<Long> getFollowingCount(@PathVariable Long userId) {
        return ResponseEntity.ok(relationshipService.getFollowingCount(userId));
    }
} 