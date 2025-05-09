package as.tobi.chidorispring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import as.tobi.chidorispring.entity.UserProfile;
import as.tobi.chidorispring.entity.UserRelationship;

@Repository
public interface UserRelationshipRepository extends JpaRepository<UserRelationship, Long> {
    Optional<UserRelationship> findByFollowerAndFollowing(UserProfile follower, UserProfile following);
    
    boolean existsByFollowerAndFollowing(UserProfile follower, UserProfile following);
    
    @Query("SELECT ur.following FROM UserRelationship ur WHERE ur.follower = :user")
    List<UserProfile> findFollowingByUser(UserProfile user);
    
    @Query("SELECT ur.follower FROM UserRelationship ur WHERE ur.following = :user")
    List<UserProfile> findFollowersByUser(UserProfile user);
    
    void deleteByFollowerAndFollowing(UserProfile follower, UserProfile following);
} 