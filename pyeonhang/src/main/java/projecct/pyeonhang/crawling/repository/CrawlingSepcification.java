package projecct.pyeonhang.crawling.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import projecct.pyeonhang.crawling.entity.CrawlingEntity;

import java.util.ArrayList;
import java.util.List;

public class CrawlingSepcification implements Specification<CrawlingEntity> {

    private String sourceChain;
    private String promoTypeRaw;
    private String productTypeRaw;
    private String keyword;


    public CrawlingSepcification( String sourceChain,
                                  String promoTypeRaw,
                                  String productTypeRaw,
                                  String keyword) {


        this.sourceChain = sourceChain;
        this.promoTypeRaw = promoTypeRaw;
        this.productTypeRaw = productTypeRaw;
        this.keyword = keyword;

    }

    @Override
    public Predicate toPredicate(Root<CrawlingEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();

        CrawlingEntity.PromoType promo = parsePromo(promoTypeRaw);
        CrawlingEntity.ProductType prod = parseProduct(productTypeRaw);

        if (promo != null) {
            predicates.add(cb.or(cb.isNull(root.get("promoType")), cb.equal(root.get("promoType").as(String.class), promo.name())));
        }


        if (prod != null) {
            predicates.add(cb.or(cb.isNull(root.get("productType")), cb.equal(root.get("productType").as(String.class), prod.name())));
        }

//        if (prod != null) {
//            predicates.add(cb.or(cb.isNull(root.get("productType")), cb.equal(root.get("productType").as(String.class), prod.name())));
//        }

        String q = normalizeBlankToNull(keyword);
        String likeText = "";

        if (q != null){
            likeText = "%" + q +"%";
            predicates.add(cb.or(cb.isNull(root.get("productName")), cb.like(root.get("productName"), likeText)));
        }

        String src = normalizeBlankToNull(sourceChain);

        if(src != null){
            predicates.add(cb.or(cb.isNull(root.get("sourceChain")), cb.equal(root.get("sourceChain"), src)));
        }


        return andTogether(predicates, cb);
    }


    private Predicate andTogether(List<Predicate> predicates, CriteriaBuilder cb) {
        return cb.and(predicates.toArray(new Predicate[0]));
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

    private static String normalizeBlankToNull(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isEmpty()) return null;
        if ("ALL".equalsIgnoreCase(v) || "-".equals(v) || "전체".equals(v)) return null;
        return v;
    }
}
