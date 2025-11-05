package projecct.pyeonhang.coupon.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Data
@Getter
@Setter
public class CouponRequestDTO {

    @NotBlank(message="쿠폰명을 입력해주세요")
    private String couponName;

    @NotBlank(message="쿠폰 설명을 입력해주세요")
    private String description;


    private Integer requiredPoint;

    private MultipartFile file;

}
