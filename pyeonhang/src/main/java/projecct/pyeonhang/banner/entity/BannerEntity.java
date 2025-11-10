package projecct.pyeonhang.banner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import projecct.pyeonhang.common.entity.BaseTimeEntity;

@Entity
@Getter
@Setter
@Table(name="banner")
public class BannerEntity extends BaseTimeEntity {

    @Id
    private String bannerId;

    private String title;

    private String linkUrl;

    private String imgUrl;

    private Integer bannerOrder;

    @Column( columnDefinition = "CHAR(1)")
    private String useYn;

}
