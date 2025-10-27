package board.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="craw_product")
public class BoardEntity {
    @Id
    private int crawlId;

    private String sourceChain;

    private String productName;

    private int price;

    private String imageUrl;

    private Enum promoType;

}
