package projecct.pyeonhang.crawling.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import projecct.pyeonhang.crawling.entity.CrawlingEntity;

@Data
@Getter
@Setter
public class CrawlingRequestDTO {

        private Integer crawlId;

        private String productName;

        private String sourceChain;

        private Integer price;

        private String imageUrl;

        private CrawlingEntity.PromoType promoType;

        private CrawlingEntity.ProductType productType;



}
