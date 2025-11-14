package projecct.pyeonhang.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardCommentRequest {
    @NotBlank(message="댓글을 입력해주세요")
    private String contents;
}
