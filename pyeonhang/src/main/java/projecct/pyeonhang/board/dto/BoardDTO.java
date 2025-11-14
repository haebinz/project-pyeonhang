package projecct.pyeonhang.board.dto;


import lombok.*;

import projecct.pyeonhang.board.entity.BoardEntity;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoardDTO {

    private int brdId;
    private String title;
    private String contents;
    private int likeCount;
    private String userId;
    private String bestYn;
    private LocalDateTime lastModifiedDate;

    public static BoardDTO of(BoardEntity entity){
        LocalDateTime last =
                entity.getUpdateDate() == null ? entity.getCreateDate() : entity.getUpdateDate();
        return BoardDTO.builder()
                .brdId(entity.getBrdId())
                .title(entity.getTitle())
                .contents(entity.getContents())
                .likeCount(entity.getLikeCount())
                .bestYn(entity.getBestYn())
                .lastModifiedDate(last)
                .build();
    }


}
