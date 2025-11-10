// projecct.pyeonhang.board.entity.BoardLike.java
package projecct.pyeonhang.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import projecct.pyeonhang.users.entity.UsersEntity;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "board_like",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_board_like", columnNames = {"brd_id", "user_id"})
        }
)
@Getter
@Setter
public class BoardLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_like_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brd_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UsersEntity user;

    @Column(name = "create_date")
    private LocalDateTime createDate = LocalDateTime.now();
}
