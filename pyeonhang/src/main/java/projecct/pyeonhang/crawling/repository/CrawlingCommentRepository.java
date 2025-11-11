package projecct.pyeonhang.crawling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projecct.pyeonhang.crawling.entity.CrawlingCommentEntity;

import java.util.List;


public interface CrawlingCommentRepository extends JpaRepository<CrawlingCommentEntity,Integer> {

    List<CrawlingCommentEntity> findByCrawling_CrawlId(Integer crawlId);


}
