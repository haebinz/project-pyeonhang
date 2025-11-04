package projecct.pyeonhang.crawling.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.*;
import projecct.pyeonhang.crawling.entity.CrawlingEntity;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access=AccessLevel.PROTECTED)
public class CrawlingDTO {

    public enum PromoType {ONE_PLUS_ONE,TWO_PLUS_ONE,GIFT,NONE}

    private Integer crawlId;
    private String sourceChain;
    private String productName;
    private Integer price;
    private String imageUrl;
    private CrawlingEntity.PromoType promoType;
    private Integer likeCount;
    private CrawlingEntity.ProductType productType;


    public static CrawlingDTO of(CrawlingEntity entity) {

        return CrawlingDTO.builder()
                .crawlId(entity.getCrawlId())
                .sourceChain(entity.getSourceChain())
                .productName(entity.getProductName())
                .price(entity.getPrice())
                .imageUrl(entity.getImageUrl())
                .promoType(entity.getPromoType())
                .likeCount(entity.getLikeCount())
                .productType(entity.getProductType())
                .build();

    }
}
