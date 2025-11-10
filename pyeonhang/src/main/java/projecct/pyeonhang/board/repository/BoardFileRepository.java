// com.convenience.board.repository.BoardFileRepository.java
package com.convenience.board.repository;

import com.convenience.board.entity.Board;
import com.convenience.board.entity.BoardFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardFileRepository extends JpaRepository<BoardFile, Integer> {
    List<BoardFile> findByBoard(Board board);
}
