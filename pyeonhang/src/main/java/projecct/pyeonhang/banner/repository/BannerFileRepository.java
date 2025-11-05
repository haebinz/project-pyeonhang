package projecct.pyeonhang.banner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projecct.pyeonhang.banner.entity.BannerFileEntity;

public interface BannerFileRepository extends JpaRepository<BannerFileEntity,Integer> {
}
