package projecct.pyeonhang.banner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projecct.pyeonhang.banner.entity.BannerEntity;

import java.util.List;

public interface BannerRepository extends JpaRepository<BannerEntity,String> {

    // 모든 배너 bannerOrder순으로 정렬, 관리자 용
    List<BannerEntity> findAllByOrderByBannerOrderAsc();

    // 사용중인 배너 bannerOrder순으로 정렬
    List<BannerEntity> findByUseYnOrderByBannerOrderAsc(String useYn);

    // 배너 노출 개수 제한
    long countByUseYn(String useYn);
}
