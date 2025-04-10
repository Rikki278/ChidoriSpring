package as.tobi.chidorispring.repository;

import as.tobi.chidorispring.entity.LikesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<LikesEntity, Long> {

}