package projecct.pyeonhang.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import projecct.pyeonhang.admin.dto.AdminUserProjection;
import projecct.pyeonhang.users.entity.UsersEntity;

import java.util.Map;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<UsersEntity,String> {

    @Query("select u.userId " +
            "from UsersEntity u " +
            "where" +
            " lower(u.userName)=lower(:userName) and lower(u.email)=lower(:email)")
    Optional<String> findUserIdByUserNameAndEmail(String userName, String email);

    Optional<UsersEntity> findByUserIdAndEmail(String userId, String email);

    @Query(value = """
    select 
        u.user_id,     
        u.user_name,   
        u.birth,        
        u.phone,        
        u.email,       
        u.nickname,     
        u.use_yn,       
        u.del_yn,     
        u.create_date,  
        u.update_date,
        u.point_balance,
        r.role_id,     
        r.role_name    
    from users u
    join user_role r on u.user_role = r.role_id
    where u.user_id = :userId
    """, nativeQuery = true)
    Optional<AdminUserProjection> getUserById(@Param("userId") String userId);


}


