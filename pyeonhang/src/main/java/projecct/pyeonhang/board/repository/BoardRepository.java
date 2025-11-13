package projecct.pyeonhang.board.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projecct.pyeonhang.board.entity.BoardEntity;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity,Integer> {

    @Query("""
        select b
          from BoardEntity b
         where (
                :keyword is null
             or (
                    :searchType = 'TITLE'
                and lower(b.title) like lower(concat('%', :keyword, '%'))
                )
             or (
                    :searchType = 'TITLE_CONTENTS'
                and (
                        lower(b.title)    like lower(concat('%', :keyword, '%'))
                     or lower(b.contents) like lower(concat('%', :keyword, '%'))
                )
             )
        )
    """)
    Page<BoardEntity> filterAll(
            @Param("searchType") String searchType,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
