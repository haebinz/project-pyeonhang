package projecct.pyeonhang.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projecct.pyeonhang.common.entity.BaseTimeEntity;
import projecct.pyeonhang.users.entity.UsersEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "board")
@Getter
@Setter
@NoArgsConstructor
public class Board extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brd_id")
    private Integer id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer", referencedColumnName = "user_id")
    private UsersEntity writer;

    @Column(name = "contents", columnDefinition = "LONGTEXT", nullable = false)
    private String contents;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "del_yn", columnDefinition = "CHAR(1)")
    private String delYn = "N";

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardFile> files = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardComment> comments = new ArrayList<>();

}
