package projecct.pyeonhang.crawling.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import projecct.pyeonhang.crawling.entity.CrawlingEntity;


import java.util.List;

public interface CrawlingRepository extends JpaRepository<CrawlingEntity,Integer> {

    @Query("""
      select c
        from CrawlingEntity c
       where (:sourceChain is null or lower(c.sourceChain) = lower(:sourceChain))
         and (:promoType  is null or c.promoType = :promoType)
         and (:productType is null or c.productType = :productType)
         and (:productName is null or lower(c.productName) like lower(concat('%', :productName, '%')))
    """)
    Page<CrawlingEntity> filterAll(
            @Param("sourceChain") String sourceChain,
            @Param("promoType") CrawlingEntity.PromoType promoType,
            @Param("productType") CrawlingEntity.ProductType productType,
            @Param("productName") String productName,
            Pageable pageable
    );




    // 전체에서 최신 10개
    List<CrawlingEntity> findTop10ByOrderByCrawlIdDesc();

    // 체인별
    List<CrawlingEntity> findTop10BySourceChainOrderByCrawlIdAsc(String sourceChain);

    /*
    // 특정 체인 개수
    long countBySourceChain(String sourceChain);
    Page<CrawlingEntity> findBySourceChain(String sourceChain, Pageable pageable);

    long countBySourceChainAndPromoType(String sourceChain, CrawlingEntity.PromoType promoType);
    Page<CrawlingEntity> findBySourceChainAndPromoType(
            String sourceChain, CrawlingEntity.PromoType promoType, Pageable pageable);

    Page<CrawlingEntity> findBySourceChainAndPromoTypeAndProductType(
            String sourceChain, CrawlingEntity.PromoType promoType,CrawlingEntity.ProductType productType, Pageable pageable);

    Page<CrawlingEntity> findBySourceChainAndProductType(
            String sourceChain, CrawlingEntity.ProductType productType, Pageable pageable);

    Page<CrawlingEntity> findByPromoType(
            CrawlingEntity.PromoType promoType, Pageable pageable
    );

    //제품 검색
    @Query("""
        select c
          from CrawlingEntity c
         where (:sourceChain is null or lower(c.sourceChain) = lower(:sourceChain))
           and (:productName  is null 
                or lower(c.productName) like lower(concat('%', :productName, '%')))
        """)
    Page<CrawlingEntity> searchProduct(@Param("sourceChain") String sourceChain,
                                       @Param("productName") String productName,
                                       Pageable pageable);*/


    CrawlingEntity getByCrawlId(int crawlId);






}
