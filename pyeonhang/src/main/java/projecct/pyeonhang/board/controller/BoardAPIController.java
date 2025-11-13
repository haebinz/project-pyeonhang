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
import projecct.pyeonhang.board.dto.BoardWriteRequest;
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
            @Valid @ModelAttribute BoardWriteRequest request,
            @AuthenticationPrincipal(expression = "username") String principalUserId
    ) {

        if (principalUserId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "인증되지 않음"));
        }

        try {

            Map<String, Object> resultMap = boardService.writeBoard(principalUserId, request);
            return ResponseEntity.ok(ApiResponse.ok(resultMap));

        } catch (Exception e) {
            log.info("게시글 작성 실패: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("게시글 작성 실패"));
        }
    }
}
