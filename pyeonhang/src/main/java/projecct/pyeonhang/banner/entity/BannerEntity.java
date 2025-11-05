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

    @Column( columnDefinition = "CHAR(1)")
    private String useYn;

    @OneToOne(mappedBy = "banner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private BannerFileEntity file;

    public void setFile(BannerFileEntity file) {
        if (this.file != null) this.file.setBanner(null);
        this.file = file;
        if (file != null) file.setBanner(this);
    }






}
