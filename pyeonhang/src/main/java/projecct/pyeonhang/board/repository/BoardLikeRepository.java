package projecct.pyeonhang.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projecct.pyeonhang.board.entity.BoardEntity;
import projecct.pyeonhang.board.entity.BoardLikeEntity;
import projecct.pyeonhang.users.entity.UsersEntity;

public interface BoardLikeRepository extends JpaRepository<BoardLikeEntity,Integer> {
    boolean existsByBoardAndUser(BoardEntity board, UsersEntity user);
}
