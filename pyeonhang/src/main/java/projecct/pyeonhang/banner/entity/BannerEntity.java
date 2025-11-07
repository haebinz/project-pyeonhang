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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int bannerId;

    private String title;

    private String linkUrl;

    private String imgUrl;

    private String cloudinaryId;

    @Column( columnDefinition = "CHAR(1)")
    private String useYn;

    @OneToOne(mappedBy = "banner", cascade = CascadeType.ALL)
    private BannerFileEntity bannerFile;

}
