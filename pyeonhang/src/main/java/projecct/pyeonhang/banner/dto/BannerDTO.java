package projecct.pyeonhang.banner.dto;

import lombok.*;
import projecct.pyeonhang.banner.entity.BannerEntity;
import projecct.pyeonhang.banner.entity.BannerFileEntity;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BannerDTO {

    private Integer bannerId;
    private String title;
    private String linkUrl;
    private String useYn;

    private String fileName;
    private String storedName;
    private String filePath;

    private LocalDateTime lastModifiedDate;

    public static BannerDTO of(BannerEntity entity) {
        BannerFileEntity file = entity.getFile();
        LocalDateTime last = entity.getUpdateDate() == null ? entity.getCreateDate() : entity.getUpdateDate();

        return BannerDTO.builder()
                .bannerId(entity.getBannerId())
                .title(entity.getTitle())
                .linkUrl(entity.getLinkUrl())
                .useYn(entity.getUseYn())
                .fileName(file != null ? file.getFileName() : null)
                .storedName(file != null ? file.getStoredName() : null)
                .filePath(file != null ? file.getFilePath() : null)
                .lastModifiedDate(last)
                .build();
    }

    
}
