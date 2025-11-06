package projecct.pyeonhang.banner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import projecct.pyeonhang.banner.entity.BannerEntity;

import java.util.Optional;

public interface BannerRepository extends JpaRepository<BannerEntity,Integer> {
    @Query("SELECT b FROM BannerEntity b LEFT JOIN FETCH b.bannerFile WHERE b.bannerId = :id")
    Optional<BannerEntity> findBannerWithFile(@Param("id") int id);
}
