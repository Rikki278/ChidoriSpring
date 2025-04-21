package as.tobi.chidorispring.repository;

import as.tobi.chidorispring.entity.CharacterPostComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<CharacterPostComment, Long> {
}