package projecct.pyeonhang.users.dto;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCouponDTO  {

    private int userCouponId;
    private int couponId;
    private String couponName;
    private String description;
    private Integer requiredPoint;
    private String fileName;
    private String storedName;
    private String filePath;
    private LocalDateTime acquiredAt;
}
