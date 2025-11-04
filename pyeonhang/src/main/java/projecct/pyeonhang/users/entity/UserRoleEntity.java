package projecct.pyeonhang.users.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import projecct.pyeonhang.common.entity.BaseTimeEntity;

@Entity
@Getter
@Setter
@Table(name="user_role")
public class UserRoleEntity extends BaseTimeEntity {
    @Id
    private String roleId;
    private String roleName;
    @Column( columnDefinition = "CHAR(1)")
    private String useYn;

}
