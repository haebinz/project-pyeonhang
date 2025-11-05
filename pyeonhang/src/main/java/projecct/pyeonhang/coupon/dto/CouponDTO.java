package projecct.pyeonhang.coupon.dto;


import lombok.*;
import projecct.pyeonhang.coupon.entity.CouponEntity;
import projecct.pyeonhang.coupon.entity.CouponFileEntity;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponDTO {


    private String couponName;
    private String description;
    private Integer requiredPoint;
    private String fileName;
    private String storedName;
    private String filePath;
    private LocalDateTime lastModifiedDate;

    public static CouponDTO of(CouponEntity entity, CouponFileEntity file){
        LocalDateTime last =
                entity.getUpdateDate() == null ? entity.getCreateDate() : entity.getUpdateDate();
        return CouponDTO.builder()
                .couponName(entity.getCouponName())
                .description(entity.getDescription())
                .requiredPoint(entity.getRequiredPoint())
                .fileName(file != null ? file.getFileName() : null)
                .storedName(file != null ? file.getStoredName() : null)
                .filePath(file != null ? file.getFilePath() : null)
                .lastModifiedDate(last)
                .build();
    }
}
