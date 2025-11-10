// com.convenience.board.repository.BoardCommentRepository.java
package com.convenience.board.repository;

import com.convenience.board.entity.Board;
import com.convenience.board.entity.BoardComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Integer> {

    List<BoardComment> findByBoardAndDelYnOrderByCreateDateAsc(Board board, String delYn);
}
