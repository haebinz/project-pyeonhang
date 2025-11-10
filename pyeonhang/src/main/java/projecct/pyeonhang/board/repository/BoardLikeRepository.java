// projecct.pyeonhang.board.repository.BoardLikeRepository.java
package projecct.pyeonhang.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projecct.pyeonhang.board.entity.Board;
import projecct.pyeonhang.board.entity.BoardLike;
import projecct.pyeonhang.users.entity.UsersEntity;

import java.util.Optional;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Integer> {

    boolean existsByBoardAndUser(Board board, UsersEntity user);

    Optional<BoardLike> findByBoardAndUser(Board board, UsersEntity user);

    long countByBoard(Board board); // 필요하면 재계산용
}
