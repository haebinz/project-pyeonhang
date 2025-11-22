package projecct.pyeonhang.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import projecct.pyeonhang.board.entity.BoardCloudinaryEntity;
import projecct.pyeonhang.board.entity.BoardEntity;

import java.util.List;

@Repository
public interface BoardCloudinaryRepository extends JpaRepository<BoardCloudinaryEntity,String> {


    List<BoardCloudinaryEntity> findByBoard_BrdId(int brdId);
}
