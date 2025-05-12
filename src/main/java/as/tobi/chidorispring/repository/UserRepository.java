package as.tobi.chidorispring.repository;

import as.tobi.chidorispring.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByEmail(String email);
    Optional<UserProfile> findByUsername(String username);
    List<UserProfile> findByUsernameContainingIgnoreCase(String username);
}
