package as.tobi.chidorispring.repository;

import as.tobi.chidorispring.entity.CharacterPostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<CharacterPostLike, Long> {
    boolean existsByCharacterPostIdAndUserId(Long characterPostId, Long userId);
    void deleteByCharacterPostIdAndUserId(Long characterPostId, Long userId);
}