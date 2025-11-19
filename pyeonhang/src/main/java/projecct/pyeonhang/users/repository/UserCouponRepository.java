package projecct.pyeonhang.users.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import projecct.pyeonhang.users.entity.UserCouponEntity;

import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCouponEntity,Integer> {
    boolean existsByUser_UserIdAndCoupon_CouponId(@Param("userId")String userId, @Param("couponId")int couponId);

    @Query("select uc from UserCouponEntity uc " +
            "join fetch uc.coupon c " +
            "where uc.user.userId = :userId " +
            "order by uc.acquiredAt desc")
    List<UserCouponEntity> findAllByUserIdWithCoupon(@Param("userId") String userId);

   @Query(
        value = "SELECT uc FROM UserCouponEntity uc JOIN FETCH uc.coupon c JOIN FETCH uc.user u",
        countQuery = "SELECT COUNT(uc) FROM UserCouponEntity uc"
    )
    Page<UserCouponEntity> findAllWithAdminCoupon(Pageable pageable);


}
