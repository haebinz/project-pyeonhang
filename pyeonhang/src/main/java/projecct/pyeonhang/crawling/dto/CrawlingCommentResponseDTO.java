package projecct.pyeonhang.crawling.dto;


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
public class CrawlingCommentResponseDTO {


    private Integer commentId;
    private Integer crawlId;
    private String userId;
    private String nickname;
    private String content;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;


    public CrawlingCommentResponseDTO(Integer commentId, Integer crawlId, String userId,String nickname,
                                      String content, LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.commentId = commentId;
        this.crawlId = crawlId;
        this.userId = userId;
        this.nickname = nickname;
        this.content = content;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;

    }

}
