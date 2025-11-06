    package projecct.pyeonhang.crawling.entity;


    import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.Setter;

    @Entity
    @Getter
    @Setter
    @Table(name="craw_product")
    public class CrawlingEntity {
        public enum PromoType {ONE_PLUS_ONE,TWO_PLUS_ONE,GIFT,NONE}
        public enum ProductType {DRINK,SNACK,FOOD,LIFE,NONE}
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int crawlId;

        //브랜드
        private String sourceChain;

        //제품 이름
        private String productName;

        //제품 가격
        private int price;

        //이미지 경로
        private String imageUrl;

        //프로모션 타입
        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private CrawlingEntity.PromoType promoType;

        //제품 카테고리
        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private CrawlingEntity.ProductType productType;

        //좋아요 수(찜하기 누르면 좋아요 수 올라감)
        private int likeCount;



    }
