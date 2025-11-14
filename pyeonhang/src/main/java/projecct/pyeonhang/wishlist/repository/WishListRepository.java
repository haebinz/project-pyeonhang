package projecct.pyeonhang.wishlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import projecct.pyeonhang.wishlist.entity.WishListEntity;
import projecct.pyeonhang.wishlist.entity.WishListId;

import java.util.List;

public interface WishListRepository extends JpaRepository<WishListEntity, WishListId> {

    boolean existsByUser_UserIdAndProduct_CrawlId(@Param("userId")String userId,@Param("crawlId") Integer crawlId);

    void deleteByUser_UserIdAndProduct_CrawlId(@Param("userId")String userId,@Param("crawlId") Integer crawlId);

    @Query("select w from WishListEntity w join fetch w.product p join fetch w.user u where u.userId = :userId")
    List<WishListEntity> findAllWithProductByUserId(@Param("userId") String userId);

    List<WishListEntity> findByUser_UserId(@Param("userId")String userId);


}
