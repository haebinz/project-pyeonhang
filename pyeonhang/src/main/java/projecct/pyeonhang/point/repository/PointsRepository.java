package projecct.pyeonhang.point.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projecct.pyeonhang.point.entity.PointsEntity;

public interface PointsRepository extends JpaRepository<PointsEntity, Integer> {

    Integer id(int id);
}
