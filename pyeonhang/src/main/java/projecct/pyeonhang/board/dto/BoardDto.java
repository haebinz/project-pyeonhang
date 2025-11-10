package com.convenience.board.dto;

import com.convenience.board.entity.Board;
import com.convenience.board.entity.BoardFile;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BoardDto {
    private Integer id;
    private String title;
    private String contents;
    private String writerId;
    private String writerNickname;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private Integer likeCount;
    private int commentCount;

    private List<String> imageUrls;

    public static BoardDto from(Board b) {
        List<String> urls = b.getFiles().stream()
                .map(BoardFile::getFilePath)
                .toList();

        return BoardDto.builder()
                .id(b.getId())
                .title(b.getTitle())
                .contents(b.getContents())
                .writerId(b.getWriter() != null ? b.getWriter().getUserId() : null)
                .writerNickname(b.getWriter() != null ? b.getWriter().getNickname() : null)
                .createDate(b.getCreateDate())
                .updateDate(b.getUpdateDate())
                .likeCount(b.getLikeCount())
                .commentCount(b.getComments() !=null ? b.getComments().size() : 0)
                .imageUrls(urls)
                .build();
    }
}
