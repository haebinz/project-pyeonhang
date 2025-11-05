package projecct.pyeonhang.users.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import projecct.pyeonhang.common.entity.BaseTimeEntity;

@Entity
@Getter
@Setter
@Table(name="users")
public class UsersEntity extends BaseTimeEntity {

    @Id
    private String userId;
    private String passwd;
    private String userName;
    private String nickname;
    private String birth;
    private String phone;
    private String email;

    @Column( columnDefinition = "CHAR(1)")
    private String useYn;
    @Column( columnDefinition = "CHAR(1)")
    @ColumnDefault("N")
    private String delYn;


    private Integer pointBalance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_role")
    @ColumnDefault("ROLE_USER")
    private UserRoleEntity role;

    @PrePersist
    void applyDefaults() {
        if (useYn == null) useYn = "Y";
        if (delYn == null) delYn = "N";
        if (pointBalance == null) pointBalance = 0;
    }
}
