package projecct.pyeonhang.crawling.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlingCommentRequestDTO {
    @NotBlank(message="댓글을 입력해주세요")
    private String content;
}
