package as.tobi.chidorispring.repository;

import as.tobi.chidorispring.entity.UserFavoritePost;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserFavoritePostRepository extends JpaRepository<UserFavoritePost, Long> {
    boolean existsByUserIdAndCharacterPostId(Long userId, Long characterPostId);
    void deleteByUserIdAndCharacterPostId(Long userId, Long characterPostId);
    List<UserFavoritePost> findByUserId(Long userId);

    void deleteByCharacterPostId(Long postId);
}