package projecct.pyeonhang.wishlist.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishListId implements Serializable {

    @Column(name="user_id",nullable = false)
    private String userId;

    @Column(name="crawl_id",nullable = false)
    private int crawlId;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WishListId that)) return false;
        return crawlId == that.crawlId && Objects.equals(userId, that.userId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(userId, crawlId);
    }
}
