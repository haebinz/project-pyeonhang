// projecct.pyeonhang.board.dto.BoardCommentDto.java
package projecct.pyeonhang.board.dto;

import lombok.Builder;
import lombok.Data;
import projecct.pyeonhang.board.entity.BoardComment;

import java.time.LocalDateTime;

@Data
@Builder
public class BoardCommentDto {

    private Integer id;
    private String content;
    private String writerId;
    private String writerNickname;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private boolean mine;  // 프론트에서 수정/삭제 버튼 표시용

    public static BoardCommentDto from(BoardComment c, String currentUserId) {
        String uid = c.getUser().getUserId();
        return BoardCommentDto.builder()
                .id(c.getId())
                .content(c.getContent())
                .writerId(uid)
                .writerNickname(c.getUser().getNickname())
                .createDate(c.getCreateDate())
                .updateDate(c.getUpdateDate())
                .mine(currentUserId != null && currentUserId.equals(uid))
                .build();
    }
}
