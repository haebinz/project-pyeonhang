package projecct.pyeonhang.board.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class BoardCommentResponseDTO {

    private Integer commentId;
    private Integer brdId;
    private String userId;
    @JsonProperty("content")
    private String contents;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public BoardCommentResponseDTO(Integer commentId, Integer brdId, String userId, String contents
            ,LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.commentId = commentId;
        this.brdId = brdId;
        this.userId = userId;
        this.contents = contents;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }
}
