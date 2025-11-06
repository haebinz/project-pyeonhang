package projecct.pyeonhang.banner.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerUpdateDTO {

    private Long bannerId;

    private String title;

    private String linkUrl;

    private MultipartFile file;

    private String useYn;
}
