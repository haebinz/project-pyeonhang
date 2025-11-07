package projecct.pyeonhang.users.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import projecct.pyeonhang.coupon.entity.CouponEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name="user_coupon")
public class UserCouponEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userCouponId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UsersEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id")
    private CouponEntity coupon;

    private LocalDateTime acquiredAt;

    @PrePersist
    void prePersist() {
        if (acquiredAt == null) acquiredAt = LocalDateTime.now();
    }
}
