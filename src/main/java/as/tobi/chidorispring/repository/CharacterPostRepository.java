package as.tobi.chidorispring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import as.tobi.chidorispring.entity.CharacterPost;

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

    @Query("SELECT p FROM CharacterPost p LEFT JOIN FETCH p.likes")
    Page<CharacterPost> findAllWithLikesAndComments(Pageable pageable);

    @Query("SELECT p FROM CharacterPost p LEFT JOIN FETCH p.likes WHERE p IN (SELECT f.characterPost FROM UserFavoritePost f WHERE f.user.id = :userId)")
    List<CharacterPost> findFavoritePostsByUserId(@Param("userId") Long userId);

    Page<CharacterPost> findByUserIdInOrderByCreatedAtDesc(List<Long> userIds, Pageable pageable);

    Page<CharacterPost> findByUserIdNotInOrderByCreatedAtDesc(List<Long> userIds, Pageable pageable);

    List<CharacterPost> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT cp FROM CharacterPost cp WHERE cp.anime IN :animeList")
    List<CharacterPost> findByAnimeIn(@org.springframework.data.repository.query.Param("animeList") List<String> animeList);

    @Query("SELECT cp FROM CharacterPost cp JOIN cp.animeGenre g WHERE g IN :genres")
    List<CharacterPost> findByAnimeGenreIn(@org.springframework.data.repository.query.Param("genres") List<String> genres);

    @Query("SELECT cp FROM CharacterPost cp WHERE cp.characterName IN :characterNames")
    List<CharacterPost> findByCharacterNameIn(@org.springframework.data.repository.query.Param("characterNames") List<String> characterNames);

    @Query("SELECT cp FROM CharacterPost cp " +
            "LEFT JOIN FETCH cp.animeGenre " +
            "WHERE cp.id = :id")
    Optional<CharacterPost> findByIdWithGenres(@Param("id") Long id);

    @Query(value = "SELECT g.anime_genre FROM character_post_genres g WHERE g.character_post_id = :postId", nativeQuery = true)
    List<String> findGenresByCharacterPostId(@Param("postId") Long postId);
}