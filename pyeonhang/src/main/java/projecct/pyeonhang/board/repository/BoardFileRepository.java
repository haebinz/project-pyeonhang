// projecct.pyeonhang.board.repository.BoardFileRepository.java
package projecct.pyeonhang.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projecct.pyeonhang.board.entity.Board;
import projecct.pyeonhang.board.entity.BoardFile;

import java.util.List;

public interface BoardFileRepository extends JpaRepository<BoardFile, Integer> {
    List<BoardFile> findByBoard(Board board);
}
