package projecct.pyeonhang.banner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projecct.pyeonhang.banner.entity.BannerEntity;

public interface BannerRepository extends JpaRepository<BannerEntity,Integer> {
}
