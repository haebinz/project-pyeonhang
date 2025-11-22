package projecct.pyeonhang.board.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projecct.pyeonhang.board.dto.BoardCommentRequest;
import projecct.pyeonhang.board.dto.BoardCommentResponseDTO;
import projecct.pyeonhang.board.entity.BoardCommentEntity;
import projecct.pyeonhang.board.entity.BoardEntity;
import projecct.pyeonhang.board.repository.BoardCommentRepository;
import projecct.pyeonhang.board.repository.BoardRepository;
import projecct.pyeonhang.users.entity.UsersEntity;
import projecct.pyeonhang.users.repository.UsersRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardCommentService {

    private final BoardRepository boardRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final UsersRepository usersRepository;

    //게시판 댓글 가져오오기
    @Transactional(readOnly = true)
    public Map<String,Object> listCommentByBoardId(Integer brdId) {
        List<BoardCommentEntity> list = boardCommentRepository
                .findByBoard_BrdId(brdId);

        List<BoardCommentResponseDTO> items = list.stream()
                .map(b->BoardCommentResponseDTO.builder()
                        .commentId(b.getCommentId())
                        .brdId(b.getBoard().getBrdId())
                        .userId(b.getUser().getUserId())
                        .contents(b.getContents())
                        .createdDate(b.getCreateDate())
                        .updatedDate(b.getUpdateDate())
                        .build())
                .collect(Collectors.toList());

        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("items",items);
        resultMap.put("count",items.size());
        resultMap.put("content",items);
        return resultMap;

    }

    //게시판 상세에 댓글 추가
    @Transactional
    public Map<String,Object> addComment(Integer brdId, String userId, BoardCommentRequest request){
        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("로그인이 필요합니다 로그인해주세요:"+userId));

        BoardEntity board = boardRepository.findById(brdId)
                .orElseThrow(()->new IllegalArgumentException("해당 게시물이 없습니다:"+brdId));

        BoardCommentEntity comment = new BoardCommentEntity();
        comment.setContents(request.getContents());
        comment.setUser(user);
        comment.setBoard(board);

        BoardCommentEntity saved = boardCommentRepository.save(comment);

        BoardCommentResponseDTO responseDTO = BoardCommentResponseDTO.builder()
                .commentId(saved.getCommentId())
                .brdId(saved.getBoard().getBrdId())
                .userId(saved.getUser().getUserId())
                .contents(saved.getContents())
                .createdDate(saved.getCreateDate())
                .updatedDate(saved.getUpdateDate())
                .build();

        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("resultCode", 200);
        resultMap.put("resultMessage", "댓글 등록 성공");
        resultMap.put("comment",responseDTO);
        return resultMap;
    }

    //게시판 댓글 수정->로그인한 사용자 기준
    @Transactional
    public Map<String,Object> updateComment(Integer commentId,String requestUserId,String newContent){
        Map<String,Object> resultMap =  new HashMap<>();

        BoardCommentEntity comment = boardCommentRepository.findById(commentId)
                .orElseThrow(()->new IllegalArgumentException("댓글이 없습니다:"+commentId));

        if(!comment.getUser().getUserId().equals(requestUserId)){
            resultMap.put("resultCode", 403);
            resultMap.put("resultMessage", "FORBIDDEN");
            return resultMap;
        }
        comment.setContents(newContent);

        resultMap.put("resultCode", 200);
        resultMap.put("resultMessage", "UPDATED");
        resultMap.put("수정된 댓글",comment.getContents());
        resultMap.put("commentId", commentId);

        return resultMap;
    }

    //게시판 댓글 삭제(자기꺼 삭제하기)
    @Transactional
    public Map<String,Object> delteComment(Integer commentId,String requestUserId){
        Map<String,Object> resultMap = new HashMap<>();

        BoardCommentEntity comment = boardCommentRepository.findById(commentId)
                .orElseThrow(()->new IllegalArgumentException("해당 댓글 없음:"+commentId));

        String user = comment.getUser().getUserId();
        if(!user.equals(requestUserId)){
            resultMap.put("resultCode", 403);
            resultMap.put("resultMessage", "FORBIDDEN");
            return resultMap;
        }

        //실제 삭제(db에서 제거)
        boardCommentRepository.delete(comment);
        resultMap.put("resultCode", 200);
        resultMap.put("resultMessage", "DELETED");
        resultMap.put("commentId", commentId);
        return resultMap;
    }
}
