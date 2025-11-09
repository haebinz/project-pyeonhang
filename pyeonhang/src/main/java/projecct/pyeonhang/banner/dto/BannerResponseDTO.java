package projecct.pyeonhang.banner.dto;

import lombok.*;
import projecct.pyeonhang.banner.entity.BannerEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerResponseDTO {

    private String bannerId;

    private String title;

    private String linkUrl;

    private String imgUrl;

    private String useYn;

    private Integer bannerOrder;

    public static BannerResponseDTO of(BannerEntity entity) {

        return BannerResponseDTO.builder()
                .bannerId(entity.getBannerId())
                .title(entity.getTitle())
                .linkUrl(entity.getLinkUrl())
                .imgUrl(entity.getImgUrl())
                .useYn(entity.getUseYn())
                .bannerOrder(entity.getBannerOrder())
                .build();
    }
}
