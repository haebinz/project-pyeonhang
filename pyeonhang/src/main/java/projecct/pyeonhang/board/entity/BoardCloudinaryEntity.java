package projecct.pyeonhang.board.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "board_cloudinary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardCloudinaryEntity {

    @Id
    private String cloudinaryId;

    private String imgUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brd_id", nullable = false)
    private BoardEntity board;
}
