// projecct.pyeonhang.board.entity.BoardComment.java
package projecct.pyeonhang.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import projecct.pyeonhang.common.entity.BaseTimeEntity;
import projecct.pyeonhang.users.entity.UsersEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "board_comment")
@Getter
@Setter
public class BoardComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brd_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UsersEntity user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "del_yn", columnDefinition = "CHAR(1)")
    private String delYn = "N";
}

