package projecct.pyeonhang.crawling.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import projecct.pyeonhang.common.dto.ApiResponse;
import projecct.pyeonhang.crawling.dto.CrawlingCommentRequestDTO;
import projecct.pyeonhang.crawling.dto.CrawlingRequestDTO;
import projecct.pyeonhang.crawling.entity.CrawlingEntity;
import projecct.pyeonhang.crawling.repository.CrawlingRepository;
import projecct.pyeonhang.crawling.service.CrawlingCommentService;
import projecct.pyeonhang.crawling.service.CrawlingService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class CrawlingAPIController {

    private final CrawlingService crawlingService;
    private final CrawlingRepository crawlingRepository;
    private final CrawlingCommentService crawlingCommentService;

    //제품 정보 가져오기
    @GetMapping({
            "/crawl",
            "/crawl/{sourceChain}",
            "/crawl/{sourceChain}/{promoType}",
            "/crawl/{sourceChain}/{promoType}/{productType}"
    })
    public ResponseEntity<ApiResponse<Map<String,Object>>> getUnified(
            @PathVariable(required = false) String sourceChain,
            @PathVariable(required = false) String promoType,
            @PathVariable(required = false) String productType,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortField", defaultValue = "price") String sortField,
            @RequestParam(name = "dir", defaultValue = "asc") String dir
    ) {

        Set<String> allowed = Set.of("price", "likeCount"); // 필요하면 필드 추가

        if (!allowed.contains(sortField)) {
            sortField = "price";
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;

        // Pageable 생성 (기본-price asc)
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Map<String,Object> result = crawlingService.getByUnifiedFilters(
                sourceChain, promoType, productType, q, pageable
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    //원하는 행사유형 입력 ex)crawl/promo/ONE_PLUS_ONE
    @GetMapping("/crawl/promo/{promoType}")
    public ResponseEntity<ApiResponse<Map<String,Object>>> getCrawlingByPromoType(
            @PathVariable CrawlingEntity.PromoType promoType,
            @PageableDefault(size=5,page=0,
                    sort="price",
                    direction=Sort.Direction.ASC) Pageable pageable){
        Map<String,Object> resultMap= crawlingService.getCrawlingByPromoType(promoType,pageable);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultMap));
    }



    //제품 상세(댓글 추가)
    @GetMapping("/crawl/detail/{crawlId}")
    public ResponseEntity<ApiResponse<Map<String,Object>>> getCrawling(@PathVariable int crawlId) throws Exception {
        Map<String,Object> resultMap = crawlingService.getProductDetail(crawlId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultMap));

    }

    //상품 수정->제품 아이디 기준으로 수정(crawId기준)
    @PatchMapping(value = "/crawl/{crawlId}", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> updateCrawl(
            @PathVariable("crawlId") int crawlId,
            @RequestBody CrawlingRequestDTO dto
    ) throws Exception {
        dto.setCrawlId(crawlId);
        Map<String, Object> resultMap = crawlingService.updateCrawlingProduct(dto);
        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    //삭제(crawlId에 따라)
    @DeleteMapping("/crawl/{crawlId}")
    public ResponseEntity<Map<String, Object>> deleteCrawl(
            @PathVariable int crawlId) {

        Map<String, Object> resultMap = crawlingService.deleteCrawlingProduct(crawlId);
        int code = (int) resultMap.getOrDefault("resultCode", 200);
        HttpStatus status = switch (code) {
            case 200 -> HttpStatus.OK;
            case 404 -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status).body(resultMap);
    }


    // 제품 댓글 등록 (로그인 필요)
    @PostMapping("/crawl/{crawlId}/comment")
    public ResponseEntity<Map<String,Object>> addComment(
            @PathVariable Integer crawlId,
            @AuthenticationPrincipal(expression = "username") String principalUserId,
            @Valid @ModelAttribute CrawlingCommentRequestDTO dto) {

        Map<String,Object> resultMap = crawlingCommentService.addComment(crawlId, principalUserId, dto);
        int code = (int) resultMap.getOrDefault("resultCode", 500);
        HttpStatus status = (code == 200) ? HttpStatus.CREATED : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(resultMap);
    }
    //댓글 수정
    @PutMapping("/crawl/comment/{commentId}")
    public ResponseEntity<Map<String, Object>> updateComment(
            @PathVariable Integer commentId,
            @AuthenticationPrincipal(expression = "username") String principalUserId,
            @RequestParam("content") String content   // form-data(x-www-form-urlencoded) 또는 query param
    ) {
        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("resultCode", 401, "resultMessage", "UNAUTHORIZED"));
        }

        Map<String, Object> resultMap = crawlingCommentService.updateComment(commentId, principalUserId, content);
        int code = (int) resultMap.getOrDefault("resultCode", 500);
        HttpStatus status = (code == 200) ? HttpStatus.OK
                : (code == 403) ? HttpStatus.FORBIDDEN
                : (code == 404) ? HttpStatus.NOT_FOUND
                : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(resultMap);
    }

    // 제품 댓글 삭제 (작성자 본인댓글 삭제, - 로그인 필요)
    @DeleteMapping("/crawl/comment/{commentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteComment(
            @PathVariable Integer commentId,
            @AuthenticationPrincipal(expression = "username") String principalUserId) {

        if (principalUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.ok(Map.of("resultCode",401,"resultMessage","UNAUTHORIZED")));
        }
        Map<String, Object> resultMap = crawlingCommentService.deleteComment(commentId, principalUserId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultMap));
    }





}
