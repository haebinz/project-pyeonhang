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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // 위시 저장
        wishListRepository.save(entity);

        // ★ 트리거 없이 직접 증가
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

    @Transactional(readOnly = true)
    public Map<String, Object> listMyWish(String userId) {
        List<WishListEntity> list = wishListRepository.findByUser_UserId(userId);

        var items = list.stream().map(w -> {
            CrawlingEntity c = w.getProduct();
            Map<String, Object> m = new HashMap<>();
            m.put("crawlId", c.getCrawlId());
            m.put("productName", c.getProductName());
            m.put("price", c.getPrice());
            m.put("imageUrl", c.getImageUrl());
            m.put("promoType", c.getPromoType());
            m.put("productType", c.getProductType());
            m.put("sourceChain", c.getSourceChain());
            m.put("likeCount", c.getLikeCount());
            return m;
        }).toList();

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("resultCode", 200);
        resultMap.put("content", items);
        resultMap.put("count", items.size());
        return resultMap;
    }


    @Transactional
    public Map<String, Object> removeWish(String userId, int crawlId) {
        Map<String,Object> res = new HashMap<>();

        // 존재 여부 체크 (선택) — 없으면 조용히 OK로 가거나 에러로 처리할지 정책 선택
        boolean exists = wishListRepository.existsByUser_UserIdAndProduct_CrawlId(userId, crawlId);
        if (!exists) {
            // 없는 경우도 OK 처리(멱등성) — 프론트 편함
            res.put("resultCode", 200);
            res.put("resultMessage", "ALREADY_REMOVED_OR_NOT_FOUND");
            res.put("crawlId", crawlId);
            // like_count는 내리지 않으므로 조회만 (선택)
            Integer likeCount = crawlingRepository.getLikeCount(crawlId);
            res.put("likeCount", likeCount);
            return res;
        }

        // 실제 삭제
        wishListRepository.deleteByUser_UserIdAndProduct_CrawlId(userId, crawlId);

        // like_count는 줄이지 않음(요구사항)
        Integer likeCount = crawlingRepository.getLikeCount(crawlId); // 참고용

        res.put("resultCode", 200);
        res.put("resultMessage", "REMOVED");
        res.put("crawlId", crawlId);
        res.put("likeCount", likeCount);
        return res;
    }
}
