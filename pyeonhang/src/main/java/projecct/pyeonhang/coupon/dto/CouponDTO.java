package projecct.pyeonhang.coupon.dto;


import lombok.*;
import projecct.pyeonhang.coupon.entity.CouponEntity;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponDTO {

    private int couponId;
    private String couponName;
    private String description;
    private Integer requiredPoint;
    private String imgUrl;
    private String cloudinaryId;
    private LocalDateTime lastModifiedDate;

    public static CouponDTO of(CouponEntity entity){
        LocalDateTime last =
                entity.getUpdateDate() == null ? entity.getCreateDate() : entity.getUpdateDate();
        return CouponDTO.builder()
                .couponId(entity.getCouponId())
                .couponName(entity.getCouponName())
                .description(entity.getDescription())
                .requiredPoint(entity.getRequiredPoint())
                .imgUrl(entity.getImgUrl())
                .cloudinaryId(entity.getCloudinaryId())
                .lastModifiedDate(last)
                .build();
    }
}
