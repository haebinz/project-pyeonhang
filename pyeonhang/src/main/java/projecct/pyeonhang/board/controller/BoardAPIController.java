package projecct.pyeonhang.board.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import projecct.pyeonhang.board.dto.BoardCloudinaryRequestDTO;
import projecct.pyeonhang.board.dto.BoardCommentRequest;
import projecct.pyeonhang.board.dto.BoardWriteRequest;
import projecct.pyeonhang.board.service.BoardCommentService;
import projecct.pyeonhang.board.service.BoardService;
import projecct.pyeonhang.common.dto.ApiResponse;

import java.util.Map;
import java.util.Set;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BoardAPIController {

    private final BoardService boardService;
    private final BoardCommentService boardCommentService;



    // 게시글 리스트 + 검색
    @GetMapping("/board")
    public ResponseEntity<ApiResponse<Object>> getBoardList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortField", defaultValue = "brdId") String sortField,  // 등록순
            @RequestParam(name = "dir", defaultValue = "desc") String dir,               // 내림차순(최신순)
            @RequestParam(name = "searchType", required = false) String searchType,
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        // 정렬 가능한 필드 제한: 등록순(brdId), 추천순(likeCount)
        Set<String> allowed = Set.of("brdId","likeCount");
        if (!allowed.contains(sortField)) {
            sortField = "brdId";
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(dir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        try {
            Map<String, Object> res = boardService.getBoardList(pageable, searchType, keyword);
            return ResponseEntity.ok(ApiResponse.ok(res));
        } catch (Exception e) {
            log.info("게시글 리스트 가져오기 실패: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("게시글 리스트 가져오기 실패"));
        }
    }


    @PostMapping("/board")
    public ResponseEntity<ApiResponse<Object>> writeBoard(
            @Valid @ModelAttribute BoardWriteRequest writeRequest,
            @Valid @ModelAttribute BoardCloudinaryRequestDTO cloudinaryRequest,
            @AuthenticationPrincipal(expression = "username") String principalUserId
    ) {

        if (principalUserId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "인증되지 않음"));
        }

        try {

            Map<String, Object> resultMap = boardService.writeBoard(principalUserId,writeRequest,cloudinaryRequest);
            return ResponseEntity.ok(ApiResponse.ok(resultMap));

        } catch (Exception e) {
            log.info("게시글 작성 실패: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("게시글 작성 실패"));
        }
    }

    @GetMapping("/board/{brdId}")
    public ResponseEntity<ApiResponse<Object>> getBoardDetail(@PathVariable int brdId) {

        try {
            Map<String, Object> resultMap = boardService.getBoardDetail(brdId);
            return ResponseEntity.ok(ApiResponse.ok(resultMap));
        } catch (RuntimeException e) {
            log.info("게시글 상세 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), "게시글을 찾을 수 없습니다."));
        }
    }

    @DeleteMapping("/board/{brdId}")
    public ResponseEntity<ApiResponse<Object>> deleteBoard(@PathVariable Integer brdId,
                                                           @AuthenticationPrincipal(expression = "username") String principalUserId) {

        if (principalUserId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "로그인이 필요합니다."));
        }

        try {
            Map<String,Object> resultMap = boardService.deleteBoard(principalUserId, brdId);
            return ResponseEntity.ok(ApiResponse.ok(resultMap));
        } catch (RuntimeException e) {
            log.info("게시글 삭제 실패: {}", e.getMessage(), e);

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("게시글 삭제 실패(서버 오류): {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "게시글을 삭제 할 수 없습니다"));
        }
    }




    //게시판 댓글 등록(로그인 필요)
    @PostMapping("/board/{brdId}/comment")
    public ResponseEntity<ApiResponse<Object>> writeComment(
            @PathVariable Integer brdId,
            @AuthenticationPrincipal(expression = "username") String principalUserId,
            @Valid @ModelAttribute BoardCommentRequest commentRequest){

        Map<String,Object> resultMap = boardCommentService.addComment(brdId,principalUserId,commentRequest);
        int code = (int) resultMap.getOrDefault("resultCode", 500);
        HttpStatus status = (code == 200) ? HttpStatus.CREATED : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(ApiResponse.ok(resultMap));

    }

    //게시판 댓글 수정
    @PutMapping("board/comment/{commentId}")
    public ResponseEntity<ApiResponse<Object>> updateComment(
            @PathVariable Integer commentId,
            @AuthenticationPrincipal(expression = "username") String principalUserId,
            @RequestParam("contents") String contents
    ){
        if(principalUserId == null) {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail(403));
        }
        Map<String,Object> resultMap = boardCommentService.updateComment(commentId,principalUserId,contents);
        int code = (int) resultMap.getOrDefault("resultCode", 500);
        HttpStatus status = (code == 200) ? HttpStatus.OK
                : (code == 403) ? HttpStatus.FORBIDDEN
                : (code == 404) ? HttpStatus.NOT_FOUND
                : HttpStatus.INTERNAL_SERVER_ERROR;
       return  ResponseEntity.status(status).body(ApiResponse.ok(resultMap));
    }

    //게시판 댓글 삭제(작성자 본인 댓글 삭제, 로그인필요)
    @DeleteMapping("board/comment/{commentId}")
    public ResponseEntity<ApiResponse<Object>> deleteComment(
            @PathVariable Integer commentId,
            @AuthenticationPrincipal(expression = "username") String principalUserId){
        if(principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail(403));
        }
        Map<String,Object> resultMap = boardCommentService.delteComment(commentId,principalUserId);
        return  ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultMap));
    }

}
