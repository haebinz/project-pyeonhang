package projecct.pyeonhang.point.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import projecct.pyeonhang.point.entity.PointsEntity;

import java.util.List;

public interface PointsRepository extends JpaRepository<PointsEntity, Integer> {

    @Query("""
      select p
      from PointsEntity p
      join fetch p.user u
      where u.userId = :userId
      order by p.id desc
    """)
    List<PointsEntity> findAllByUserIdWithUser(@Param("userId")String userId);

    @Query("select coalesce(sum(p.amount),0) from PointsEntity p where p.user.userId = :userId")
    Integer sumByUserId(@Param("userId")String userId);
    Integer id(@Param("id")int id);
}
