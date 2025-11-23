package projecct.pyeonhang.board.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import projecct.pyeonhang.board.entity.BoardEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity,Integer> {


    @Query("""
    select b
      from BoardEntity b
      join b.user u
      join u.role r
     where 
         b.tempYn = 'N'
         and(
          b.noticeYn = 'Y'
          or (
                 :keyword is null
              or (
                     :searchType = 'TITLE'
                 and lower(b.title) like lower(concat('%', :keyword, '%'))
                 )
              or (
                     :searchType = 'TITLE_CONTENTS'
                 and (
                         lower(b.title) like lower(concat('%', :keyword, '%'))
                      or lower(b.contents) like lower(concat('%', :keyword, '%'))
                 )
              )
              or(
                   :searchType = 'WRITER'
                and lower(u.userId) like lower(concat('%', :keyword, '%'))
              )
              )
          )
     order by
         
          case when b.noticeYn = 'Y' then 0 else 1 end asc,

       
          case 
              when b.noticeYn = 'Y' and upper(r.roleId) in ('ADMIN', 'ROLE_ADMIN') then 0
              when b.noticeYn = 'Y' then 1
              else 2
          end asc,

        
          case when :sortType = 'LIKE' then b.likeCount end desc,
          case when :sortType = 'CREATED' then b.brdId end desc,
            b.brdId desc
""")
    Page<BoardEntity> findBoardList(
            @Param("searchType") String searchType,
            @Param("keyword") String keyword,
            @Param("sortType") String sortType,
            Pageable pageable
    );

    List<BoardEntity> findAllByBrdIdIn(List<Integer> brdIdList);

    void deleteAllByTempYn(String tempYn);



}
