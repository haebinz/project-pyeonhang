package projecct.pyeonhang.crawling.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import projecct.pyeonhang.crawling.entity.CrawlingEntity;


import java.util.List;

public interface CrawlingRepository extends JpaRepository<CrawlingEntity,Integer> ,
                                                 JpaSpecificationExecutor<CrawlingEntity> {

    @Query(value="""
      select c
        from CrawlingEntity c
       where (:sourceChain is null or lower(c.sourceChain) = lower(:sourceChain))
         and (:promoType  is null or c.promoType = :promoType)
         and (:productType is null or c.productType = :productType)
         and (:productName is null or lower(c.productName) like lower(concat('%', :productName, '%')))
    """,
   countQuery = """
  select count(c)
    from CrawlingEntity c
   where (:sourceChain is null or lower(c.sourceChain) = lower(:sourceChain))
     and (:promoType  is null or c.promoType = :promoType)
     and (:productType is null or c.productType = :productType)
     and (:productName is null or lower(c.productName) like lower(concat('%', :productName, '%')))
"""
    )
    Page<CrawlingEntity> filterAll(
            @Param("sourceChain") String sourceChain,
            @Param("promoType") CrawlingEntity.PromoType promoType,
            @Param("productType") CrawlingEntity.ProductType productType,
            @Param("productName") String productName,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update CrawlingEntity c set c.likeCount = c.likeCount + 1 where c.crawlId = :id")
    int increaseLikeCount(@Param("id") int crawlId);

    // 현재 like_count 가져오기(선택)
    @Query("select c.likeCount from CrawlingEntity c where c.crawlId = :id")
    Integer getLikeCount( @Param("id")int crawlId);


    // 체인별
    List<CrawlingEntity> findTop10BySourceChainOrderByCrawlIdAsc(@Param("sourceChain") String sourceChain);

    Page<CrawlingEntity> findByPromoType(
            CrawlingEntity.PromoType promoType, Pageable pageable
    );

    List<CrawlingEntity> findTop5ByOrderByLikeCountDesc();








}
