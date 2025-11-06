package projecct.pyeonhang.crawling.service;


import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projecct.pyeonhang.common.utils.FileUtils;
import projecct.pyeonhang.crawling.dto.CrawlingDTO;
import projecct.pyeonhang.crawling.dto.CrawlingRequestDTO;
import projecct.pyeonhang.crawling.entity.CrawlingEntity;
import projecct.pyeonhang.crawling.repository.CrawlingRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrawlingService {


    private final CrawlingRepository crawlingRepository;

    public Map<String, Object> getByUnifiedFilters(
            String sourceChain,
            String promoTypeRaw,
            String productTypeRaw,
            String keyword,
            Pageable pageable
    ) {
        String src = normalizeBlankToNull(sourceChain);

        CrawlingEntity.PromoType promo = parsePromo(promoTypeRaw);
        CrawlingEntity.ProductType prod = parseProduct(productTypeRaw);

        String q = normalizeBlankToNull(keyword);
        if (q != null) q = q.trim();

        Page<CrawlingEntity> page = crawlingRepository.filterAll(src, promo, prod, q, pageable);

        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("sourceChain", src);
        resultMap.put("promoType", promo);
        resultMap.put("productType", prod);
        resultMap.put("query", q);
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

    /*// 전체 가져오기
    public Map<String,Object> getCrawlingAll() {
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("totalCount", crawlingRepository.count());
        resultMap.put("latest10", crawlingRepository.findTop10ByOrderByCrawlIdDesc()
                .stream()
                .map(CrawlingDTO::of)
                .collect(Collectors.toList())
        );
        return resultMap;
    }*/

    //전체 가져오기(전체 상품)
    /*public Map<String,Object> getAll( Pageable pageable){
        Map<String,Object> resultMap = new HashMap<>();
        Page<CrawlingEntity> pageResult = crawlingRepository.findAll(pageable);
        List<CrawlingDTO> items = pageResult.getContent()
                .stream()
                .map(CrawlingDTO::of)
                .collect(Collectors.toList());
        resultMap.put("items",items);
        resultMap.put("totalPages",pageResult.getTotalPages());
        resultMap.put("currentPage",pageable.getPageNumber());
        resultMap.put("pageSize",pageable.getPageSize());
        return resultMap;
    }

    

    // 체인별 상세 (체인별 제품 count + 전체)
    public Map<String,Object> getCrawlingBySourceChain(String sourceChain, Pageable pageable) {
        Map<String,Object> resultMap = new HashMap<>();

        Page<CrawlingEntity> pageResult = crawlingRepository.findBySourceChain(sourceChain, pageable);

        // DTO 변환
        List<CrawlingDTO> items = pageResult.getContent()
                .stream()
                .map(CrawlingDTO::of)
                .collect(Collectors.toList());

        resultMap.put("sourceChain", sourceChain);
        resultMap.put("totalElements", pageResult.getTotalElements());
        resultMap.put("totalPages", pageResult.getTotalPages());
        resultMap.put("items", items);

        return resultMap;
    }

    //체인별 전체->카테고리 선택
    public Map<String,Object> getCategoryOfTotal(String sourceChain,CrawlingEntity.ProductType productType,Pageable pageable) {
        Map<String,Object> resultMap = new HashMap<>();
        Page<CrawlingEntity> pageResult = crawlingRepository.findBySourceChainAndProductType(sourceChain,productType,pageable);
        // DTO 변환
        List<CrawlingDTO> items = pageResult.getContent()
                .stream()
                .map(CrawlingDTO::of)
                .collect(Collectors.toList());

        resultMap.put("sourceChain", sourceChain);
        resultMap.put("totalElements", pageResult.getTotalElements());
        resultMap.put("totalPages", pageResult.getTotalPages());
        resultMap.put("items", items);

        return resultMap;

    }

    //체인->행사유형별 구분
    public Map<String, Object> getByChainAndPromo(
            String sourceChain,
            @Nullable CrawlingEntity.PromoType promo,  // null이면 전체
            Pageable pageable
    ) {
        Map<String,Object> resultMap = new HashMap<>();
        Page<CrawlingEntity> pageResult = crawlingRepository.findBySourceChainAndPromoType(sourceChain,promo, pageable);

        List<CrawlingDTO> items = pageResult.getContent()
                .stream()
                .map(CrawlingDTO::of)
                .collect(Collectors.toList());

        resultMap.put("sourceChain", sourceChain);
        resultMap.put("totalElements", pageResult.getTotalElements());
        resultMap.put("totalPages", pageResult.getTotalPages());
        resultMap.put("items", items);

        return resultMap;
    }


/*
*  public Map<String,Object> getCrawlingByPromoType(String sourceChain,CrawlingEntity.PromoType promoType,Pageable pageable) {
        Map<String,Object> resultMap = new HashMap<>();

        Page<CrawlingEntity> pageResult = crawlingRepository.findBySourceChainAndPromoType(sourceChain ,promoType, pageable);
        // DTO 변환
        List<CrawlingDTO> items = pageResult.getContent()
                .stream()
                .map(CrawlingDTO::of)
                .collect(Collectors.toList());

        resultMap.put("sourceChain", sourceChain);
        resultMap.put("totalElements", pageResult.getTotalElements());
        resultMap.put("totalPages", pageResult.getTotalPages());
        resultMap.put("items", items);

        return resultMap;
    }*/
    /*
    //행사 유형별 가져오기
    public Map<String,Object> getCrawlingByPromoType(CrawlingEntity.PromoType promoType,Pageable pageable) {
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

    //카테고리별 가져오기
    public Map<String,Object> getCrawlingBySourceAndProductType(String sourceChain,
                                                                CrawlingEntity.PromoType promoType,
                                                                CrawlingEntity.ProductType productType,
                                                                Pageable pageable) {
        Map<String,Object> resultMap = new HashMap<>();

        Page<CrawlingEntity> pageResult = crawlingRepository.findBySourceChainAndPromoTypeAndProductType(sourceChain ,promoType,productType, pageable);
        // DTO 변환
        List<CrawlingDTO> items = pageResult.getContent()
                .stream()
                .map(CrawlingDTO::of)
                .collect(Collectors.toList());

        resultMap.put("sourceChain", sourceChain);
        resultMap.put("totalElements", pageResult.getTotalElements());
        resultMap.put("totalPages", pageResult.getTotalPages());
        resultMap.put("items", items);

        return resultMap;
    }*/







    //트렌잭션 처리->업데이트 안되면 반영 X
    @Transactional
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
    public Map<String,Object> deleteCrawlingProduct(int crawlId) {
        Map<String,Object> resultMap = new HashMap<>();

        if (!crawlingRepository.existsById(crawlId)) {
            resultMap.put("resultCode", 404);
            resultMap.put("message", "crawlId를 찾을 수 없음 " + crawlId);
            return resultMap;
        }

        try {
            crawlingRepository.deleteById(crawlId); // 하드 삭제
            resultMap.put("resultCode", 200);
            resultMap.put("deletedId", crawlId);
        } catch(Exception e) {
            resultMap.put("resultCode", 500);
        }

        return resultMap;
    }
    /*
    //제품 검색
    public Map<String, Object> searchProducts(String sourceChain, String keyword, Pageable pageable) {
        String src = (sourceChain == null || sourceChain.isBlank()) ? null : sourceChain.trim();
        String productName   = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Page<CrawlingEntity> page = crawlingRepository.searchProduct(src, productName, pageable);

        Map<String,Object> result = new HashMap<>();
        result.put("sourceChain", src);
        result.put("query", productName);
        result.put("totalElements", page.getTotalElements());
        result.put("totalPages", page.getTotalPages());
        result.put("items", page.getContent().stream().map(CrawlingDTO::of).toList());
        return result;
    }*/

    public Map<String,Object> getProductDetail(int crawlId){
        Map<String,Object> resultMap = new HashMap<>();
        CrawlingEntity entity = crawlingRepository.findById(crawlId).orElseThrow(()-> new IllegalArgumentException("crawlId not found: " + crawlId));
        resultMap.put("sourceChain", entity.getSourceChain());
        resultMap.put("productName", entity.getProductName());
        resultMap.put("price", entity.getPrice());
        resultMap.put("imageUrl", entity.getImageUrl());
        resultMap.put("promoType", entity.getPromoType());
        resultMap.put("productType", entity.getProductType());
        resultMap.put("likeCount", entity.getLikeCount());

        return resultMap;
    }





    // 태스트용-체인별 최신 10개만
    public List<CrawlingDTO> getLatest10BySourceChain(String sourceChain) {
        return crawlingRepository
                .findTop10BySourceChainOrderByCrawlIdAsc(sourceChain)
                .stream()
                .map(CrawlingDTO::of)
                .collect(Collectors.toList());
    }


}
