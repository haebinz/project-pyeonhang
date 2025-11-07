// com.convenience.board.service.BoardCommentService.java
package com.convenience.board.service;

import com.convenience.board.dto.BoardCommentDto;
import com.convenience.board.entity.Board;
import com.convenience.board.entity.BoardComment;
import com.convenience.board.repository.BoardCommentRepository;
import com.convenience.board.repository.BoardRepository;
import com.convenience.user.entity.User;
import com.convenience.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardCommentService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardCommentRepository boardCommentRepository;

    @Transactional(readOnly = true)
    public List<BoardCommentDto> list(Integer boardId, String currentUserId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

        List<BoardComment> comments =
                boardCommentRepository.findByBoardAndDelYnOrderByCreateDateAsc(board, "N");

        return comments.stream()
                .map(c -> BoardCommentDto.from(c, currentUserId))
                .toList();
    }

    @Transactional
    public BoardCommentDto create(Integer boardId, String userId, String content) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        BoardComment c = new BoardComment();
        c.setBoard(board);
        c.setUser(user);
        c.setContent(content);
        c.setDelYn("N");
        c.setCreateDate(LocalDateTime.now());

        BoardComment saved = boardCommentRepository.save(c);
        return BoardCommentDto.from(saved, userId);
    }

    @Transactional
    public BoardCommentDto update(Integer commentId, String userId, boolean isAdmin, String content) {
        BoardComment c = boardCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));

        String writerId = c.getUser().getUserId();
        if (!writerId.equals(userId) && !isAdmin) {
            throw new AccessDeniedException("댓글 수정 권한이 없습니다.");
        }

        c.setContent(content);
        c.setUpdateDate(LocalDateTime.now());

        return BoardCommentDto.from(c, userId);
    }

    @Transactional
    public void delete(Integer commentId, String userId, boolean isAdmin) {
        BoardComment c = boardCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));

        String writerId = c.getUser().getUserId();
        if (!writerId.equals(userId) && !isAdmin) {
            throw new AccessDeniedException("댓글 삭제 권한이 없습니다.");
        }

        c.setDelYn("Y");
        c.setUpdateDate(LocalDateTime.now());
    }
}
