package projecct.pyeonhang.crawling.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projecct.pyeonhang.common.dto.ApiResponse;
import projecct.pyeonhang.crawling.dto.CrawlingRequestDTO;
import projecct.pyeonhang.crawling.entity.CrawlingEntity;
import projecct.pyeonhang.crawling.repository.CrawlingRepository;
import projecct.pyeonhang.crawling.service.CrawlingService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class CrawlingAPIController {

    private final CrawlingService crawlingService;
    private final CrawlingRepository crawlingRepository;

    @GetMapping("/crawl") //전체 가져오기 -> 화면에 뜨는지 보기위해서 현재는 10개만 표시하도록
    public ResponseEntity<Map<String, Object>> getCrawlingAll() throws Exception {
        Map<String, Object> resultMap = crawlingService.getCrawlingAll();
        resultMap.put("총개수", crawlingRepository.count());
        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    

    //체인별  api/v1/crawl/chain/{sourceChain}
    //체인별 전체 상품
    @GetMapping("/crawl/chain/{sourceChain}")
    public ResponseEntity<ApiResponse<Map<String,Object>>> getChain(@PathVariable String sourceChain,
                                                                    @PageableDefault(size=20,page=0,
                                                                    sort="price",
                                                                            direction = Sort.Direction.ASC) Pageable pageable) throws Exception {
        Map<String,Object> resultMap = crawlingService.getCrawlingBySourceChain(sourceChain,pageable);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultMap));
    }
    //체인별 전체 상품->카테고리 고르기
    @GetMapping("/crawl/{sourceChain}/promo/{productType}")
    public ResponseEntity<ApiResponse<Map<String,Object>>> getChainTotal(@PathVariable String sourceChain,
                                                                         @PathVariable CrawlingEntity.ProductType productType,
                                                                         @PageableDefault(size=20,page=0,
                                                                                 sort="price",direction =  Sort.Direction.ASC)Pageable pageable) throws Exception {
        Map<String,Object> resultMap = crawlingService.getCategoryOfTotal(sourceChain,productType,pageable);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultMap));

    }

    //체인별 제품->행사 유형별
    @GetMapping("/crawl/{sourceChain}/{promoType}")
    public ResponseEntity<ApiResponse<Map<String,Object>>> getChainByPromoType(@PathVariable String sourceChain,
                                                                               @PathVariable CrawlingEntity.PromoType promoType,
                                                                               @PageableDefault(size=20,page=0,
                                                                            sort="price",
                                                                            direction = Sort.Direction.ASC) Pageable pageable) throws Exception {
        Map<String,Object> resultMap = crawlingService.getByChainAndPromo(sourceChain,promoType,pageable);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultMap));
    }



    //체인별제품->행사유형->카테고리
    @GetMapping("/crawl/{sourceChain}/{promoType}/{productType}")
    public ResponseEntity<ApiResponse<Map<String,Object>>> getCategory(@PathVariable String sourceChain,
                                                                       @PathVariable CrawlingEntity.PromoType promoType,
                                                                       @PathVariable CrawlingEntity.ProductType productType,
                                                                       @PageableDefault(size=20,page=0,
                                                                               sort="price",
                                                                               direction=Sort.Direction.ASC) Pageable pageable) throws Exception {
        Map<String,Object> resultMap = crawlingService.getCrawlingBySourceAndProductType(sourceChain,promoType,productType,pageable);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultMap));
    }
    //제품 검색
    @GetMapping("/crawl/{sourceChain}/search")
    public ResponseEntity<ApiResponse<Map<String,Object>>> searchInChain(
            @PathVariable String sourceChain,
            @RequestParam(name = "productName", required = false) String productName,
            @PageableDefault(size=20, page=0, sort="price", direction=Sort.Direction.ASC)
            Pageable pageable) {

        Map<String,Object> result = crawlingService.searchProducts(sourceChain, productName, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    //제품 상세
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

    @GetMapping("/crawl/promo/{promoType}")
    public ResponseEntity<ApiResponse<Map<String,Object>>> getCrawlingByPromoType(
            @PathVariable CrawlingEntity.PromoType promoType,
            @PageableDefault(size=20,page=0,
                    sort="price",
                    direction=Sort.Direction.ASC) Pageable pageable){
        Map<String,Object> resultMap= crawlingService.getCrawlingByPromoType(promoType,pageable);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultMap));
    }






}
