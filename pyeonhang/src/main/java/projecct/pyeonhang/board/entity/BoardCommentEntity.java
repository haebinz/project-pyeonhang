package projecct.pyeonhang.board.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Repository;
import projecct.pyeonhang.common.entity.BaseTimeEntity;
import projecct.pyeonhang.users.entity.UsersEntity;

@Entity
@Table(name="board_comment")
@Getter
@Setter
public class BoardCommentEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int commentId;

    @Lob
    @Column(name = "contents",columnDefinition = "TEXT")
    private String contents;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name="brd_id")
    private BoardEntity board;

    @ManyToOne(fetch=FetchType.LAZY,optional = false)
    @JoinColumn(name="user_id")
    private UsersEntity user;


}
