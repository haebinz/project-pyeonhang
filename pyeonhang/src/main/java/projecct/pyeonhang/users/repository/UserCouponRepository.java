package projecct.pyeonhang.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import projecct.pyeonhang.users.entity.UserCouponEntity;

import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCouponEntity,Integer> {
    boolean existsByUser_UserIdAndCoupon_CouponId(String userId, int couponId);

    @Query("select uc from UserCouponEntity uc " +
            "join fetch uc.coupon c " +
            "where uc.user.userId = :userId " +
            "order by uc.acquiredAt desc")
    List<UserCouponEntity> findAllByUserIdWithCoupon(String userId);

    @Query("SELECT uc FROM UserCouponEntity uc JOIN FETCH uc.coupon")
    List<UserCouponEntity> findAllWithCoupon();
}
