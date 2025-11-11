package projecct.pyeonhang.crawling.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class CrawlingCommentResponseDTO {


    private Integer commentId;
    private Integer crawlId;
    private String userId;
    private String content;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;


    public CrawlingCommentResponseDTO(Integer commentId, Integer crawlId, String userId,
                                      String content, LocalDateTime createDate, LocalDateTime updateDate) {
        this.commentId = commentId;
        this.crawlId = crawlId;
        this.userId = userId;
        this.content = content;
        this.createDate = createDate;
        this.updateDate = updateDate;

    }

}
