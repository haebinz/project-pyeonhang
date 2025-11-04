package projecct.pyeonhang.crawling.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCrawlingEntity is a Querydsl query type for CrawlingEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCrawlingEntity extends EntityPathBase<CrawlingEntity> {

    private static final long serialVersionUID = -1309897127L;

    public static final QCrawlingEntity crawlingEntity = new QCrawlingEntity("crawlingEntity");

    public final NumberPath<Integer> crawlId = createNumber("crawlId", Integer.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final NumberPath<Integer> likeCount = createNumber("likeCount", Integer.class);

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final StringPath productName = createString("productName");

    public final EnumPath<CrawlingEntity.ProductType> productType = createEnum("productType", CrawlingEntity.ProductType.class);

    public final EnumPath<CrawlingEntity.PromoType> promoType = createEnum("promoType", CrawlingEntity.PromoType.class);

    public final StringPath sourceChain = createString("sourceChain");

    public QCrawlingEntity(String variable) {
        super(CrawlingEntity.class, forVariable(variable));
    }

    public QCrawlingEntity(Path<? extends CrawlingEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCrawlingEntity(PathMetadata metadata) {
        super(CrawlingEntity.class, metadata);
    }

}

