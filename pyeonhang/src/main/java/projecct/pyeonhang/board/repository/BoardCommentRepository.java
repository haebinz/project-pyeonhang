package projecct.pyeonhang.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import projecct.pyeonhang.board.entity.BoardCommentEntity;

import java.util.List;

@Repository
public interface BoardCommentRepository extends JpaRepository<BoardCommentEntity,Integer> {

    List<BoardCommentEntity> findByBoard_BrdId(int brdId);
}
