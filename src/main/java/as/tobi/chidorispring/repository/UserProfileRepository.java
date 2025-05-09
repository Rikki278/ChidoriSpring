package as.tobi.chidorispring.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import as.tobi.chidorispring.entity.UserProfile;

@Repository
public interface  UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUsername(String username);
    Optional<UserProfile> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
} 