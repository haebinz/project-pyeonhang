package projecct.pyeonhang.point.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import projecct.pyeonhang.common.entity.BaseTimeEntity;
import projecct.pyeonhang.users.entity.UsersEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="points")
public class PointsEntity extends BaseTimeEntity {
    public enum SourceType {ADMIN_GRANT,ATTENDANCE,COUPON_EXCHANGE}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    //사용자 아이디
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UsersEntity user;

    //포인트 지급 유형
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceType sourceType;

    //증감 포인트(+적립, -차감)
    private int amount;
    //포인트 지급 사유
    private String reason;







}
