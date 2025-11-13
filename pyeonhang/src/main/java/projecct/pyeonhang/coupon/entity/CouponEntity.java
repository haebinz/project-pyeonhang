package projecct.pyeonhang.coupon.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import projecct.pyeonhang.common.entity.BaseTimeEntity;

@Entity
@Getter
@Setter
@Table(name="coupon")
public class CouponEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int couponId;

    private String couponName;

    private String description;

    private int requiredPoint;

    private String cloudinaryId;

    private String imgUrl;

}
