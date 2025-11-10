// projecct.pyeonhang.board.repository.BoardCommentRepository.java
package projecct.pyeonhang.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projecct.pyeonhang.board.entity.Board;
import projecct.pyeonhang.board.entity.BoardComment;

import java.util.List;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Integer> {

    List<BoardComment> findByBoardAndDelYnOrderByCreateDateAsc(Board board, String delYn);
}
