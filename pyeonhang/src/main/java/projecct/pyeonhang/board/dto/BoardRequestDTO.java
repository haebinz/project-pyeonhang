package projecct.pyeonhang.board.dto;

import lombok.Builder;
import lombok.Data;
import projecct.pyeonhang.board.entity.Board;


@Data
@Builder
public class BoardRequestDTO {
    private Integer id;
    private String title;
    private String contents;

    public static BoardDto from(Board b) {
        return BoardDto.builder()
                .id(b.getId())
                .title(b.getTitle())
                .contents(b.getContents())
                .build();
    }
}
