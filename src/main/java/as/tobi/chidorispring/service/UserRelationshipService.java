package as.tobi.chidorispring.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import as.tobi.chidorispring.dto.userProfile.UserProfileShortDTO;
import as.tobi.chidorispring.entity.UserProfile;
import as.tobi.chidorispring.entity.UserRelationship;
import as.tobi.chidorispring.mapper.UserMapper;
import as.tobi.chidorispring.repository.UserProfileRepository;
import as.tobi.chidorispring.repository.UserRelationshipRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserRelationshipService {
    private final UserRelationshipRepository relationshipRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;

    @Transactional
    public void followUser(UserProfile follower, Long followingId) {
        UserProfile following = userProfileRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("User to follow not found"));

        if (follower.getId().equals(followingId)) {
            throw new RuntimeException("Cannot follow yourself");
        }

        if (relationshipRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new RuntimeException("Already following this user");
        }

        UserRelationship relationship = UserRelationship.builder()
                .follower(follower)
                .following(following)
                .build();

        relationshipRepository.save(relationship);
    }

    @Transactional
    public void unfollowUser(UserProfile follower, Long followingId) {
        UserProfile following = userProfileRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("User to unfollow not found"));

        relationshipRepository.deleteByFollowerAndFollowing(follower, following);
    }

    public List<UserProfileShortDTO> getFollowers(Long userId) {
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return relationshipRepository.findFollowersByUser(user).stream()
                .map(userMapper::toUserProfileShortDto)
                .collect(Collectors.toList());
    }

    public List<UserProfileShortDTO> getFollowing(Long userId) {
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return relationshipRepository.findFollowingByUser(user).stream()
                .map(userMapper::toUserProfileShortDto)
                .collect(Collectors.toList());
    }

    public boolean isFollowing(UserProfile follower, Long followingId) {
        UserProfile following = userProfileRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("User to check not found"));
        return relationshipRepository.existsByFollowerAndFollowing(follower, following);
    }

    public long getFollowersCount(Long userId) {
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return relationshipRepository.findFollowersByUser(user).size();
    }

    public long getFollowingCount(Long userId) {
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return relationshipRepository.findFollowingByUser(user).size();
    }
} 