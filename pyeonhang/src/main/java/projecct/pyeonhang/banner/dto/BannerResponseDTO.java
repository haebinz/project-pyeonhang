package projecct.pyeonhang.banner.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import projecct.pyeonhang.banner.entity.BannerEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerResponseDTO {

    private int bannerId;

    private String title;

    private String linkUrl;

    private String imgUrl;

    private String useYn;

    public static BannerResponseDTO of(BannerEntity entity) {

        return BannerResponseDTO.builder()
                .bannerId(entity.getBannerId())
                .title(entity.getTitle())
                .linkUrl(entity.getLinkUrl())
                .imgUrl(entity.getBannerFile().getStoredName())
                .useYn(entity.getUseYn())
                .build();
    }
}
