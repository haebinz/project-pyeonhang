package projecct.pyeonhang.board.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import projecct.pyeonhang.board.dto.BoardCommentDto;
import projecct.pyeonhang.board.dto.BoardDto;
import projecct.pyeonhang.board.dto.BoardRequestDTO;
import projecct.pyeonhang.board.service.BoardCommentService;
import projecct.pyeonhang.board.service.BoardService;
import projecct.pyeonhang.common.dto.ApiResponse;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/board")
public class BoardController {
    private final BoardService boardService;
    private final BoardCommentService boardCommentService;

    // 게시글 목록, 검색 및 정렬 api/board?pageize=10=0&s
    // 공개 api 이기에 토큰 필요 X
    @GetMapping
    public ApiResponse<Page<BoardDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchType, // TITLE or TITLE_CONTENT
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "LATEST") String sortType // LATEST or LIKE
    ) {
        Page<BoardDto> data = boardService.getBoardPage(page, size, searchType, keyword, sortType);
        return ApiResponse.ok(data);
    }

    // 게시글 상세 api/board/{{게시글 ID}}
    // 게시글 목록과 마찬가지로 토큰 X
    @GetMapping("/{id}")
    public ApiResponse<BoardDto> get(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails user) {
        BoardDto dto = boardService.getBoard(id);
        return ApiResponse.ok(dto);
    }

    // 게시글 등록 api/board
    // Authorization: Bearer token, Content-Type: application/json
    // { "title": "테스트 글", "contents": "내용입니다" }
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Object>> createWithFile(
            @Valid @ModelAttribute BoardRequestDTO request,
            @AuthenticationPrincipal UserDetails user) throws Exception {

        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("로그인이 필요합니다."));
            try {
                boardService.create(request, user.getUsername());
                return ResponseEntity.ok(ApiResponse.ok("OK"));
            } catch(Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("게시글 등록 실패"));
            }
    }

    // 게시글 수정 api/board/{{게시글 ID}}
    // Authorization: Bearer token, Content-Type: application/json
    // { "title": "수정된 제목", "contents": "수정된 내용" }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> update(
            @PathVariable Integer id,
            @RequestParam("title") String title,
            @RequestParam("contents") String contents,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserDetails user) throws Exception {
        if (user == null)
            return ApiResponse.fail("로그인이 필요합니다.");
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boardService.update(id, title, contents, user.getUsername(), isAdmin, file);
        return ApiResponse.ok(null);
    }

    // 게시글 삭제 api/board/{{게시글 ID}}
    // Authorization: Bearer token
    @DeleteMapping("/{id}")
    public ApiResponse<Object> delete(@PathVariable Integer id, @AuthenticationPrincipal UserDetails user) {
        if (user == null)
            return ApiResponse.fail("로그인이 필요합니다.");
        boolean isAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boardService.delete(id, user.getUsername(), isAdmin);
        return ApiResponse.ok(null);
    }

    // 게시글 추천
    @PostMapping("/{id}/like")
    public ApiResponse<Object> like(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal UserDetails user) {
        if (user == null) {
            return ApiResponse.fail(500, "로그인이 필요합니다.");
        }

        int likeCount = boardService.like(id, user.getUsername());
        return ApiResponse.ok(likeCount);
    }

    // 댓글 목록 조회 (상세 페이지 진입 시)
    @GetMapping("/{id}/comments")
    public ApiResponse<List<BoardCommentDto>> comments(
            @PathVariable("id") Integer boardId,
            @AuthenticationPrincipal UserDetails user) {
        String currentUserId = (user != null ? user.getUsername() : null);
        List<BoardCommentDto> list = boardCommentService.list(boardId, currentUserId);
        return ApiResponse.ok(list);
    }

    // 댓글 작성
    @PostMapping("/{id}/comments")
    public ApiResponse<Object> createComment(
            @PathVariable("id") Integer boardId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails user) {
        if (user == null)
            return ApiResponse.fail("로그인이 필요합니다.");

        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ApiResponse.fail("댓글 내용을 입력해주세요.");
        }

        BoardCommentDto dto = boardCommentService.create(boardId, user.getUsername(), content);
        return ApiResponse.ok(dto);
    }

    // 댓글 수정
    @PutMapping("/{boardId}/comments/{commentId}")
    public ApiResponse<Object> updateComment(
            @PathVariable Integer boardId,
            @PathVariable Integer commentId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails user) {
        if (user == null)
            return ApiResponse.fail("로그인이 필요합니다.");
        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ApiResponse.fail("댓글 내용을 입력해주세요.");
        }

        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        BoardCommentDto dto = boardCommentService.update(commentId, user.getUsername(), isAdmin, content);
        return ApiResponse.ok(dto);
    }

    // 댓글 삭제
    @DeleteMapping("/{boardId}/comments/{commentId}")
    public ApiResponse<Object> deleteComment(
            @PathVariable Integer boardId,
            @PathVariable Integer commentId,
            @AuthenticationPrincipal UserDetails user) {
        if (user == null)
            return ApiResponse.fail("로그인이 필요합니다.");

        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boardCommentService.delete(commentId, user.getUsername(), isAdmin);
        return ApiResponse.ok(null);
    }
}
