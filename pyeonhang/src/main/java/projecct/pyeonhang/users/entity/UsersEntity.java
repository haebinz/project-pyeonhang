package projecct.pyeonhang.users.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import projecct.pyeonhang.common.entity.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name="users")
public class UsersEntity extends BaseTimeEntity {

    @Id
    @Column(name = "user_id")
    private String userId;
    private String passwd;
    private String userName;
    private String nickname;
    private String birth;
    private String phone;
    private String email;

    @Column( columnDefinition = "CHAR(1)")
    @ColumnDefault("N")
    private String delYn;


    private Integer pointBalance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_role")
    @ColumnDefault("USER")
    private UserRoleEntity role;

    /*@OneToMany(mappedBy = "writer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> boards = new ArrayList<>();*/


    @PrePersist
    void applyDefaults() {
        if (delYn == null) delYn = "N";
        if (pointBalance == null) pointBalance = 0;
        if (nickname == null) {this.nickname = this.userId;}
    }

}
