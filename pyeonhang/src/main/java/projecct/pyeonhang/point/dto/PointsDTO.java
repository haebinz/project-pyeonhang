package projecct.pyeonhang.point.dto;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PointsDTO {

    private int id;

    private int amount;

    private String reason;

    private String sourceType;

    private LocalDateTime createDate;



}
