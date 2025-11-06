package projecct.pyeonhang.wishlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import projecct.pyeonhang.wishlist.entity.WishListEntity;
import projecct.pyeonhang.wishlist.entity.WishListId;

import java.util.List;

public interface WishListRepository extends JpaRepository<WishListEntity, WishListId> {

    boolean existsByUser_UserIdAndProduct_CrawlId(String userId, Integer crawlId);

    void deleteByUser_UserIdAndProduct_CrawlId(String userId, Integer crawlId);

    List<WishListEntity> findByUser_UserId(String userId);


}
