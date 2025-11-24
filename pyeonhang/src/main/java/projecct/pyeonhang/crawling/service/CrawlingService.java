package projecct.pyeonhang.crawling.service;


import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projecct.pyeonhang.common.utils.FileUtils;
import projecct.pyeonhang.crawling.dto.CrawlingDTO;
import projecct.pyeonhang.crawling.dto.CrawlingRequestDTO;
import projecct.pyeonhang.crawling.entity.CrawlingEntity;
import projecct.pyeonhang.crawling.repository.CrawlingRepository;
import projecct.pyeonhang.crawling.repository.CrawlingSepcification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrawlingService {


    private final CrawlingRepository crawlingRepository;
    private final CrawlingCommentService crawlingCommentService;

//    @Cacheable(
//            value = "productList",
//            key = "#sourceChain + '-' + #promoTypeRaw + '-' + #productTypeRaw + '-' + #keyword + '-' + " +
//                    "(#pageable != null ? #pageable.pageNumber : 0) + '-' + " +
//                    "(#pageable != null ? #pageable.pageSize : 20)",
//            condition = "#pageable != null"
//    )
    public Map<String, Object> getByUnifiedFilters(
            String sourceChain,
            String promoTypeRaw,
            String productTypeRaw,
            String keyword,
            Pageable pageable
    ) {
        if (pageable == null) {
        pageable = PageRequest.of(0, 20);
    }
        System.out.println("캐싱실행");

        CrawlingSepcification crawlingSepcification =
                new CrawlingSepcification(sourceChain, promoTypeRaw, productTypeRaw, keyword);
        Page<CrawlingEntity> page = null;
        try {
             page = crawlingRepository.findAll(crawlingSepcification, pageable);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        Map<String,Object> resultMap = new HashMap<>();
//        resultMap.put("sourceChain", src);
//        resultMap.put("promoType", promo);
//        resultMap.put("productType", prod);
//        resultMap.put("query", q);
        resultMap.put("totalElements", page.getTotalElements());
        resultMap.put("totalPages", page.getTotalPages());
        resultMap.put("currentPage", pageable.getPageNumber());
        resultMap.put("pageSize", pageable.getPageSize());
        resultMap.put("items", page.getContent().stream().map(CrawlingDTO::of).toList());
        return resultMap;
    }

    private static String normalizeBlankToNull(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isEmpty()) return null;
        if ("ALL".equalsIgnoreCase(v) || "-".equals(v) || "전체".equals(v)) return null;
        return v;
    }

    private static CrawlingEntity.PromoType parsePromo(String raw) {
        String v = normalizeBlankToNull(raw);
        if (v == null) return null;
        if ("전체".equals(v)) return null; // 필터 의미로만 사용
        return CrawlingEntity.PromoType.valueOf(v);
    }

    private static CrawlingEntity.ProductType parseProduct(String raw) {
        String v = normalizeBlankToNull(raw);
        if (v == null) return null;
        return CrawlingEntity.ProductType.valueOf(v);
    }

    //행사 유형별 가져오기
    @Cacheable(value="promoList")
    public Map<String,Object> getCrawlingByPromoType(CrawlingEntity.PromoType promoType,Pageable pageable) {
        System.out.println("메인페이지 캐싱 실행");
        Map<String,Object> resultMap = new HashMap<>();

        Page<CrawlingEntity> pageResult = crawlingRepository.findByPromoType(promoType, pageable);
        // DTO 변환
        List<CrawlingDTO> items = pageResult.getContent()
                .stream()
                .map(CrawlingDTO::of)
                .collect(Collectors.toList());

        resultMap.put("totalElements", pageResult.getTotalElements());
        resultMap.put("totalPages", pageResult.getTotalPages());
        resultMap.put("items", items);

        return resultMap;
    }




    //트렌잭션 처리->업데이트 안되면 반영 X
    //제품수정
    @Transactional
    @CacheEvict(value = {"productList", "promoList"}, allEntries = true)
    public Map<String,Object> updateCrawlingProduct(CrawlingRequestDTO dto) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        CrawlingEntity entity = crawlingRepository.findById(dto.getCrawlId())
                .orElseThrow(() -> new IllegalArgumentException("crawlId not found: " + dto.getCrawlId()));

        //업데이트 할때 변경된 부분 없으면 그대로
        if (dto.getProductName() != null)   entity.setProductName(dto.getProductName());
        if (dto.getSourceChain() != null)   entity.setSourceChain(dto.getSourceChain());
        if (dto.getProductType() != null)   entity.setProductType(dto.getProductType());
        if (dto.getPrice() != null)         entity.setPrice(dto.getPrice());
        if (dto.getImageUrl() != null)      entity.setImageUrl(dto.getImageUrl());
        if (dto.getPromoType() != null)     entity.setPromoType(dto.getPromoType());


        crawlingRepository.save(entity);

        resultMap.put("resultCode", 200);
        resultMap.put("updated", CrawlingDTO.of(entity));
        return resultMap;
    }

    //제품 삭제
    @Transactional
    @CacheEvict(value = {"productList", "promoList"}, allEntries = true)
    public Map<String,Object> deleteCrawlingProduct(int crawlId) {
        Map<String,Object> resultMap = new HashMap<>();

        if (!crawlingRepository.existsById(crawlId)) {
            resultMap.put("resultCode", 404);
            resultMap.put("message", "crawlId를 찾을 수 없음 " + crawlId);
            return resultMap;
        }

        try {
            crawlingRepository.deleteById(crawlId); // db에서 삭제
            resultMap.put("resultCode", 200);
            resultMap.put("deletedId", crawlId);
        } catch(Exception e) {
            resultMap.put("resultCode", 500);
        }

        return resultMap;
    }

    //제품 상세+댓글
    @Transactional(readOnly = true)
    public Map<String, Object> getProductDetail(int crawlId){
        Map<String, Object> resultMap = new HashMap<>();
        CrawlingEntity entity = crawlingRepository.findById(crawlId)
                .orElseThrow(() -> new IllegalArgumentException("crawlId not found: " + crawlId));

        // product info
        Map<String, Object> product = new HashMap<>();
        product.put("sourceChain", entity.getSourceChain());
        product.put("productName", entity.getProductName());
        product.put("price", entity.getPrice());
        product.put("imageUrl", entity.getImageUrl());
        product.put("promoType", entity.getPromoType());
        product.put("productType", entity.getProductType());
        product.put("likeCount", entity.getLikeCount());
        product.put("crawlId", entity.getCrawlId());
        product.put("crawledAt", entity.getCrawledAt());

        resultMap.put("product", product);

        // comments - CrawlingCommentService의 리스트 반환을 그대로 사용
        Map<String, Object> commentsMap = crawlingCommentService.listCommentsByCrawlId(crawlId);
        // commentsMap = { resultCode:200, count:N, content: [CrawlingCommentResponseDTO,...] }
        resultMap.put("comments", commentsMap.get("content"));
        resultMap.put("commentsCount", commentsMap.get("count"));

        return resultMap;
    }


    public Map<String, Object> getTop5PopularProducts() {
        Map<String, Object> result = new HashMap<>();

        List<CrawlingEntity> entities = crawlingRepository.findTop5ByOrderByLikeCountDesc();

        List<CrawlingDTO> items = entities.stream()
                .map(CrawlingDTO::of)
                .collect(Collectors.toList());

        result.put("count", items.size());
        result.put("items", items);

        return result;
    }
}
