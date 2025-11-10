package projecct.pyeonhang.wishlist.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projecct.pyeonhang.crawling.entity.CrawlingEntity;
import projecct.pyeonhang.crawling.repository.CrawlingRepository;
import projecct.pyeonhang.users.entity.UsersEntity;
import projecct.pyeonhang.users.repository.UsersRepository;
import projecct.pyeonhang.wishlist.entity.WishListEntity;
import projecct.pyeonhang.wishlist.entity.WishListId;
import projecct.pyeonhang.wishlist.repository.WishListRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishListService {

    private final WishListRepository wishListRepository;
    private final UsersRepository usersRepository;
    private final CrawlingRepository crawlingRepository;



    //찜추가
    @Transactional
    public Map<String,Object> addWish(String userId, int crawlId){
        Map<String,Object> resultMap = new HashMap<>();
        WishListId id = new WishListId(userId, crawlId);

        // 이미 있으면 증가 X
        if (wishListRepository.existsById(id)) {
            Integer likeCount = crawlingRepository.getLikeCount(crawlId); // optional
            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "이미존재하는상품입니다.");
            resultMap.put("likeCount", likeCount);
            return resultMap;
        }

        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + userId));
        CrawlingEntity product = crawlingRepository.findById(crawlId)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음(crawl): " + crawlId));

        WishListEntity entity = WishListEntity.of(user, product);
        
        wishListRepository.save(entity);

        //좋아요 증가
        int updated = crawlingRepository.increaseLikeCount(crawlId);
        if (updated != 1) {
            throw new IllegalStateException("like_count 증가 실패(crawlId=" + crawlId + ")");
        }

        Integer likeCount = crawlingRepository.getLikeCount(crawlId); // 최신값 확인용

        resultMap.put("resultCode", 200);
        resultMap.put("resultMessage", "ADDED");
        resultMap.put("crawlId", crawlId);
        resultMap.put("likeCount", likeCount);
        return resultMap;
    }
    //찜목록 가져오기
    @Transactional(readOnly = true)
    public Map<String, Object> listMyWish(String userId) {
        List<WishListEntity> list = wishListRepository.findByUser_UserId(userId);

        // Asia/Seoul 기준 현재 연도/월
        java.time.ZoneId seoul = java.time.ZoneId.of("Asia/Seoul");
        java.time.LocalDate nowSeoul = java.time.LocalDate.now(seoul);
        int currentYear = nowSeoul.getYear();
        int currentMonth = nowSeoul.getMonthValue();

        List<Map<String,Object>> items = list.stream()
                .filter(w -> {
                    CrawlingEntity crawlEntity = w.getProduct();
                    java.time.LocalDateTime crawledAt = crawlEntity.getCrawledAt();
                    if (crawledAt == null) return false; // 크롤링 날짜 없으면 제외
                    // 비교: 연도와 월이 같아야 이번 달로 간주
                    return crawledAt.getYear() == currentYear && crawledAt.getMonthValue() == currentMonth;
                })
                .map(w -> {
                    CrawlingEntity entity = w.getProduct();
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("crawlId", entity.getCrawlId());
                    resultMap.put("productName", entity.getProductName());
                    resultMap.put("price", entity.getPrice());
                    resultMap.put("imageUrl", entity.getImageUrl());
                    resultMap.put("promoType", entity.getPromoType());
                    resultMap.put("productType", entity.getProductType());
                    resultMap.put("sourceChain", entity.getSourceChain());
                    resultMap.put("likeCount", entity.getLikeCount());
                    resultMap.put("crawledAt", entity.getCrawledAt());
                    return resultMap;
                })
                .toList();

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("resultCode", 200);
        resultMap.put("content", items);
        resultMap.put("count", items.size());
        return resultMap;
    }

    //찜삭제
    @Transactional
    public Map<String, Object> removeWish(String userId, int crawlId) {
        Map<String,Object> resultMap = new HashMap<>();


        boolean exists = wishListRepository.existsByUser_UserIdAndProduct_CrawlId(userId, crawlId);
        if (!exists) {

            resultMap.put("resultCode", 200);
            resultMap.put("resultMessage", "ALREADY_REMOVED_OR_NOT_FOUND");
            resultMap.put("crawlId", crawlId);
            // like_count는 내리지 않으므로 조회만
            Integer likeCount = crawlingRepository.getLikeCount(crawlId);
            resultMap.put("likeCount", likeCount);
            return resultMap;
        }

        //삭제
        wishListRepository.deleteByUser_UserIdAndProduct_CrawlId(userId, crawlId);

        // like_count는 변동x
        Integer likeCount = crawlingRepository.getLikeCount(crawlId); // 참고용

        resultMap.put("resultCode", 200);
        resultMap.put("resultMessage", "REMOVED");
        resultMap.put("crawlId", crawlId);
        resultMap.put("likeCount", likeCount);
        return resultMap;
    }
}
