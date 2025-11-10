package projecct.pyeonhang.banner.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Getter
@Setter
public class BannerRequestDTO {
    // 수정 + 등록을 동시에 하기 위함
    // 등록 시 bannerId가 없는 경우 null값을 포함하기 위해 Integer 타입
    private String bannerId;
    @NotBlank(message="배너 제목을 입력해주세요")
    private String title;
    private String linkUrl;
    private String imgUrl;

    private Integer fileIndex;

    // @NotBlank(message="배너 이미지를 등록해주세요")
    // private MultipartFile file; // 파일은 service에서 넣어줌

    private String useYn;

}
