package projecct.pyeonhang.coupon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import projecct.pyeonhang.banner.entity.BannerEntity;
import projecct.pyeonhang.common.entity.BaseTimeEntity;

@Entity
@Getter
@Setter
@Table(name="coupon_file")
public class CouponFileEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int cfId;

    private String fileName;

    private String storedName;

    private String filePath;

    private Long fileSize;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false, unique = true) //
    private CouponEntity coupon;

}
