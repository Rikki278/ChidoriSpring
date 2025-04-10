package as.tobi.chidorispring.repository;

import as.tobi.chidorispring.entity.CharacterPost;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface CharacterPostRepository extends JpaRepository<CharacterPost, Long> {

    // Find all user posts
    List<CharacterPost> findByUserId(Long userId);

    // Find anime posts (with pagination)
    Page<CharacterPost> findByAnimeContainingIgnoreCase(String anime, Pageable pageable);

    // Check if there is a post with such ID
    boolean existsByIdAndUserId(Long postId, Long userId);

    // Find the top-n posts by the number of likes (via jpql)
    @Query("SELECT p FROM CharacterPost p LEFT JOIN p.likes l GROUP BY p ORDER BY COUNT(l) DESC")
    List<CharacterPost> findTopPopularPosts(Pageable pageable);
}