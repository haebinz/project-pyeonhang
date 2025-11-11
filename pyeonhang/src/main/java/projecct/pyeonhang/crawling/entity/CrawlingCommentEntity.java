package projecct.pyeonhang.crawling.entity;

import groovy.lang.Lazy;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import projecct.pyeonhang.common.entity.BaseTimeEntity;
import projecct.pyeonhang.users.entity.UsersEntity;

@Entity
@Getter
@Setter
@Table(name="crawling_comment")
public class CrawlingCommentEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int commentId;

    @Lob
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crawl_id")
    private CrawlingEntity crawling;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UsersEntity user;




}
