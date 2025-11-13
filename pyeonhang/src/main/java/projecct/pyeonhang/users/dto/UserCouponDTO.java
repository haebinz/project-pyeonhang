package projecct.pyeonhang.users.dto;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCouponDTO  {

    private String userId;
    private int userCouponId;
    private int couponId;
    private String couponName;
    private String description;
    private Integer requiredPoint;
    private String imgUrl;
    private LocalDateTime acquiredAt;
}
