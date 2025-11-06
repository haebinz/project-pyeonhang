package projecct.pyeonhang.banner.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import projecct.pyeonhang.common.entity.BaseTimeEntity;

@Entity
@Getter
@Setter
@Table(name="banner_file")
public class BannerFileEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int bannerFileId;

    private String fileName;

    private String storedName;

    private String filePath;

    private Long fileSize;

    @OneToOne
    @JoinColumn(name="banner_id")
    private BannerEntity banner;
}
