package projecct.pyeonhang.board.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import projecct.pyeonhang.board.entity.Board;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Integer> {

    // 삭제 안된 게시글 전체
    Page<Board> findByDelYn(String delYn, Pageable pageable);

    List<Board> findByDelYn(String delYn, Sort sort);

    // 제목으로 검색
    Page<Board> findByTitleContainingIgnoreCaseAndDelYn(
            String keyword,
            String delYn,
            Pageable pageable
    );


}
