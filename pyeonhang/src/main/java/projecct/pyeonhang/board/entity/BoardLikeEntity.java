package projecct.pyeonhang.board.entity;

import jakarta.persistence.*;
import lombok.*;
import projecct.pyeonhang.users.entity.UsersEntity;

@Entity
@Table(name="board_like")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardLikeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brd_id", nullable = false)
    private BoardEntity board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UsersEntity user;
}
