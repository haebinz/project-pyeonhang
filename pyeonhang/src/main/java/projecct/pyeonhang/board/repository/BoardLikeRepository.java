package projecct.pyeonhang.board.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import projecct.pyeonhang.board.entity.BoardEntity;
import projecct.pyeonhang.board.entity.BoardLikeEntity;
import projecct.pyeonhang.users.entity.UsersEntity;

public interface BoardLikeRepository extends JpaRepository<BoardLikeEntity,Integer> {
    // 특정 사용자가 좋아요를 눌렀는지 확인
    Optional<BoardLikeEntity> findByBoardAndUser(BoardEntity board, UsersEntity user);

    void deleteByBoard(BoardEntity board);
}
