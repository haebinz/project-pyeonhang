// com.convenience.board.repository.BoardLikeRepository.java
package com.convenience.board.repository;

import com.convenience.board.entity.Board;
import com.convenience.board.entity.BoardLike;
import com.convenience.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Integer> {

    boolean existsByBoardAndUser(Board board, User user);

    Optional<BoardLike> findByBoardAndUser(Board board, User user);

    long countByBoard(Board board); // 필요하면 재계산용
}
