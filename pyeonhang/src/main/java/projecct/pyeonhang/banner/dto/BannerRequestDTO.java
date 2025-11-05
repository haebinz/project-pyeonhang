package projecct.pyeonhang.banner.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Data
@Getter
@Setter
public class BannerRequestDTO {

    @NotBlank(message="배너 제목을 입력해주세요")
    private String title;
    @NotBlank(message="배너 URL을 입력해주세요")
    private String linkUrl;

    private MultipartFile file;

    private String useYn;

}
