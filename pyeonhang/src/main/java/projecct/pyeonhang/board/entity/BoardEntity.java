package projecct.pyeonhang.board.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import projecct.pyeonhang.common.entity.BaseTimeEntity;
import projecct.pyeonhang.users.entity.UsersEntity;

@Entity
@Table(name="board")
@Getter
@Setter
public class BoardEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int brdId;

    private String title;

    private String contents;

    private int likeCount;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UsersEntity user;

    @Column( columnDefinition = "CHAR(1)")
    private String bestYn;

    @Column( columnDefinition = "CHAR(1)")
    private String noticeYn;

    @Column(columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String tempYn;



}
