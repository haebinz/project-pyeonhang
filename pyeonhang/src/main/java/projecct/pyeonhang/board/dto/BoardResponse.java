package projecct.pyeonhang.board.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.processing.SupportedSourceVersion;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BoardResponse {

    private int brdId;
    private String title;
    private String userId;
    // private String contents;
    private int likeCount;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private String bestYn;
    private String noticeYn;
}
