package projecct.pyeonhang.coupon.dto;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponUpdateDTO {

    private Integer couponId;

    private String couponName;

    private String description;

    private String cloudinaryId;

    private String imgUrl;

    private Integer requiredPoint;

    private MultipartFile file;
}
