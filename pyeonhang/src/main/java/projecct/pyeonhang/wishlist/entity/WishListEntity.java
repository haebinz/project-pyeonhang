package projecct.pyeonhang.wishlist.entity;

import jakarta.persistence.*;
import lombok.*;
import projecct.pyeonhang.crawling.entity.CrawlingEntity;
import projecct.pyeonhang.users.entity.UsersEntity;

@Entity
@Table(name = "wish_list")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishListEntity {

    @EmbeddedId
    private WishListId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UsersEntity user;

    @MapsId("crawlId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crawl_id", nullable = false)
    private CrawlingEntity product;


    public static WishListEntity of(UsersEntity user, CrawlingEntity product) {
        return WishListEntity.builder()
                .id(WishListId.builder()
                        .userId(user.getUserId())
                        .crawlId(product.getCrawlId()) // CrawlingEntity가 int면 auto-unboxing 됨
                        .build())
                .user(user)
                .product(product)
                .build();
    }

}
