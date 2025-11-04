package projecct.pyeonhang.users.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUsersEntity is a Querydsl query type for UsersEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUsersEntity extends EntityPathBase<UsersEntity> {

    private static final long serialVersionUID = -372366263L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUsersEntity usersEntity = new QUsersEntity("usersEntity");

    public final projecct.pyeonhang.common.entity.QBaseTimeEntity _super = new projecct.pyeonhang.common.entity.QBaseTimeEntity(this);

    public final StringPath birth = createString("birth");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    public final StringPath delYn = createString("delYn");

    public final StringPath email = createString("email");

    public final StringPath nickname = createString("nickname");

    public final StringPath passwd = createString("passwd");

    public final StringPath phone = createString("phone");

    public final NumberPath<Integer> pointBalance = createNumber("pointBalance", Integer.class);

    public final QUserRoleEntity role;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateDate = _super.updateDate;

    public final StringPath userId = createString("userId");

    public final StringPath userName = createString("userName");

    public final StringPath useYn = createString("useYn");

    public QUsersEntity(String variable) {
        this(UsersEntity.class, forVariable(variable), INITS);
    }

    public QUsersEntity(Path<? extends UsersEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUsersEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUsersEntity(PathMetadata metadata, PathInits inits) {
        this(UsersEntity.class, metadata, inits);
    }

    public QUsersEntity(Class<? extends UsersEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.role = inits.isInitialized("role") ? new QUserRoleEntity(forProperty("role")) : null;
    }

}

