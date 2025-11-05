package projecct.pyeonhang.banner.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import projecct.pyeonhang.banner.entity.BannerEntity;

public interface BannerRepository extends JpaRepository<BannerEntity,Integer> {

    @EntityGraph(attributePaths = "file")
    Page<BannerEntity> findAll(Pageable pageable);
}
