package projecct.pyeonhang.board.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import projecct.pyeonhang.board.dto.BoardCloudinaryRequestDTO;
import projecct.pyeonhang.board.dto.BoardCommentRequest;
import projecct.pyeonhang.board.dto.BoardWriteRequest;
import projecct.pyeonhang.board.service.BoardCommentService;
import projecct.pyeonhang.board.service.BoardService;
import projecct.pyeonhang.common.dto.ApiResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BoardAPIController {

    private final BoardService boardService;
    private final BoardCommentService boardCommentService;

    // 게시글 가져오기 + 검색
    @GetMapping("/board")
    public ResponseEntity<ApiResponse<Object>> getBoardList(
            @RequestParam(name = "sortType", defaultValue = "create") String sortType,
            @RequestParam(name = "searchType", required = false) String searchType,
            @RequestParam(name = "keyword", required = false) String keyword,
            Pageable pageable) {

        try {
            Map<String, Object> res = boardService.getBoardList(
                    sortType,
                    searchType,
                    keyword,
                    pageable);
            return ResponseEntity.ok(ApiResponse.ok(res));
        } catch (Exception e) {
            log.info("게시글 리스트 가져오기 실패: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("게시글 리스트 가져오기 실패"));
        }
    }

    // 게시글 수정
    @PutMapping("/board/{brdId}")
    public ResponseEntity<ApiResponse<Object>> updateBoard(
            @PathVariable int brdId,
            @Valid @ModelAttribute BoardWriteRequest writeRequest,
            @ModelAttribute BoardCloudinaryRequestDTO cloudinaryRequest,
            @AuthenticationPrincipal(expression = "username") String principalUserId) {

        if (principalUserId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "인증되지 않음"));
        }

        try {
            Map<String, Object> resultMap = boardService.updateBoard(principalUserId, brdId, writeRequest,
                    cloudinaryRequest);

            int code = (int) resultMap.getOrDefault("resultCode", 500);

            HttpStatus status = switch (code) {
                case 200 -> HttpStatus.OK;
                case 403 -> HttpStatus.FORBIDDEN;
                default -> HttpStatus.INTERNAL_SERVER_ERROR;
            };

            return ResponseEntity.status(status).body(ApiResponse.ok(resultMap));

        } catch (Exception e) {
            log.info("게시글 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("게시글 수정 실패"));
        }

    }

    // 게시글 상세
    @GetMapping("/board/{brdId}")
    public ResponseEntity<ApiResponse<Object>> getBoardDetail(
        @PathVariable("brdId") int brdId, 
        @AuthenticationPrincipal Object principal
    ) {

        try {
            Map<String, Object> resultMap = boardService.getBoardDetail(brdId, principal);
            return ResponseEntity.ok(ApiResponse.ok(resultMap));
        } catch (RuntimeException e) {
            log.info("게시글 상세 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), "게시글을 찾을 수 없습니다."));
        }
    }

    // 게시글 삭제(본인이 작성한 게시글 삭제)
    @DeleteMapping("/board/{brdId}")
    public ResponseEntity<ApiResponse<Object>> deleteBoard(@PathVariable("brdId") Integer brdId,
            @AuthenticationPrincipal(expression = "username") String principalUserId) {

        if (principalUserId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "로그인이 필요합니다."));
        }

        try {
            Map<String, Object> resultMap = boardService.deleteBoard(principalUserId, brdId);
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

    // 게시판 댓글 리스트
    @GetMapping("/board/{brdId}/comment") 
    public ResponseEntity<ApiResponse<Object>> getComments(@PathVariable("brdId") Integer brdId) {
        Map<String, Object> resultMap = boardCommentService.listCommentByBoardId(brdId);

        try {
            return ResponseEntity.ok(ApiResponse.ok(resultMap));
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.fail("댓글 조회 실패"));
        }
    };

    // 게시판 댓글 등록(로그인 필요)
    @PostMapping("/board/{brdId}/comment")
    public ResponseEntity<ApiResponse<Object>> writeComment(
            @PathVariable("brdId") Integer brdId,
            @AuthenticationPrincipal(expression = "username") String principalUserId,
            @Valid @ModelAttribute BoardCommentRequest commentRequest) {

        Map<String, Object> resultMap = boardCommentService.addComment(brdId, principalUserId, commentRequest);
        int code = (int) resultMap.getOrDefault("resultCode", 500);
        HttpStatus status = (code == 200) ? HttpStatus.CREATED : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(ApiResponse.ok(resultMap));

    }

    // 게시판 댓글 수정(본인꺼)
    @PutMapping("board/comment/{commentId}")
    public ResponseEntity<ApiResponse<Object>> updateComment(
            @PathVariable("commentId") Integer commentId,
            @AuthenticationPrincipal(expression = "username") String principalUserId,
            @RequestParam("contents") String contents) {
        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail(403));
        }
        Map<String, Object> resultMap = boardCommentService.updateComment(commentId, principalUserId, contents);
        int code = (int) resultMap.getOrDefault("resultCode", 500);
        HttpStatus status = (code == 200) ? HttpStatus.OK
                : (code == 403) ? HttpStatus.FORBIDDEN
                        : (code == 404) ? HttpStatus.NOT_FOUND
                                : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(ApiResponse.ok(resultMap));
    }

    // 게시판 댓글 삭제(작성자 본인 댓글 삭제, 로그인필요)
    @DeleteMapping("board/comment/{commentId}")
    public ResponseEntity<ApiResponse<Object>> deleteComment(
            @PathVariable("commentId") Integer commentId,
            @AuthenticationPrincipal(expression = "username") String principalUserId) {
        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail(403));
        }
        Map<String, Object> resultMap = boardCommentService.delteComment(commentId, principalUserId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultMap));
    }

    // 추천 누르기
    @PostMapping("/board/{brdId}/like")
    public ResponseEntity<ApiResponse<Object>> recommendBoard(
            @PathVariable("brdId") int brdId,
            @AuthenticationPrincipal Object principal) {

        try {
            Map<String, Object> resultMap = boardService.boardRecommend(principal, brdId);
            int code = (int) resultMap.getOrDefault("resultCode", 500);

            HttpStatus status = switch (code) {
                case 200 -> HttpStatus.OK;
                case 409 -> HttpStatus.CONFLICT;
                default -> HttpStatus.INTERNAL_SERVER_ERROR;
            };

            return ResponseEntity.status(status).body(ApiResponse.ok(resultMap));

        } catch (Exception e) {
            log.info("게시글 추천 실패: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(e.getMessage()));
        }
    }

    // 임시 테이블 생성
    @PostMapping("/board/temp")
    public ResponseEntity<ApiResponse<Object>> createTempBoard(
            @AuthenticationPrincipal Object principal) {
        try {
            Map<String, Object> result = boardService.createTempBoard(principal);
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.fail(e.getMessage()));
        }
    }

    // 이미지 등록 시 cloudinary에 저장
    @PostMapping("/board/{brdId}/image")
    public ResponseEntity<ApiResponse<Object>> uploadImage(
            @PathVariable("brdId") int brdId,
            @RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = boardService.uploadBoardImage(brdId, file);
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.fail("이미지 업로드 실패"));
        }
    }

    // 글 등록(게시글 및 이미지)
    @PutMapping("/board/{brdId}/submit")
    public ResponseEntity<ApiResponse<Object>> submitBoard(
            @PathVariable("brdId") int brdId,
            @Valid @ModelAttribute BoardWriteRequest dto,
            @AuthenticationPrincipal(expression = "username") String userId) {
        try {
            Map<String, Object> result = boardService.submitBoard(brdId, userId, dto);
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.fail("게시글 등록 실패"));
        }
    }

    // 글 등록 취소 시 임시테이블 삭제
    @DeleteMapping("/board/{brdId}/cancel")
    public ResponseEntity<ApiResponse<Object>> cancelBoard(
            @PathVariable("brdId") int brdId,
            @AuthenticationPrincipal(expression = "username") String userId) {
        try {
            Map<String, Object> result = boardService.cancelBoard(brdId, userId);
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.fail("임시 게시글 삭제 실패"));
        }
    }

    // 이미지 등록했다가 빼기
    @DeleteMapping("/board/{brdId}/image/{cloudinaryId}")
    public ResponseEntity<ApiResponse<Object>> deleteBoardImage(
            @PathVariable("brdId") int brdId,
            @PathVariable("cloudinaryId") String cloudinaryId,
            @AuthenticationPrincipal(expression = "username") String userId) {
        try {
            Map<String, Object> result = boardService.deleteBoardImage(brdId, cloudinaryId, userId);
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.fail("이미지 삭제 실패"));
        }
    }
}
