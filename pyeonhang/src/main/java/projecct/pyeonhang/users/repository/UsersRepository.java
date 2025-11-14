package projecct.pyeonhang.users.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import projecct.pyeonhang.admin.dto.AdminUserProjection;
import projecct.pyeonhang.users.entity.UsersEntity;

import java.util.Map;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<UsersEntity,String> {

    // 아이디 중복 확인
    boolean existsByUserId(@Param("userId")String userId);
    
    @Query("select u.userId " +
            "from UsersEntity u " +
            "where" +
            " lower(u.userName)=lower(:userName) and lower(u.email)=lower(:email)")
    Optional<String> findUserIdByUserNameAndEmail(@Param("userName")String userName, @Param("email")String email);

    Optional<UsersEntity> findByUserIdAndEmail(@Param("userId")String userId, @Param("email")String email);

    @Query(value = """
    select 
        u.user_id,     
        u.user_name,   
        u.birth,        
        u.phone,        
        u.email,       
        u.nickname,          
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


    @Modifying
    @Transactional
    @Query("update UsersEntity u set u.pointBalance = u.pointBalance - :amount where u.userId = :userId and u.pointBalance >= :amount")
    int decrementPointBalanceIfEnough(@Param("userId") String userId, @Param("amount") int amount);


    @Query("""
        select u
        from UsersEntity u
        where (:role is null or :role = 'ALL' or u.role.roleId = :role)
          and (:delYn is null or :delYn = 'ALL' or u.delYn = :delYn)
          and (
                :search is null
                or lower(u.userId) like concat('%', lower(:search), '%')
                or lower(u.userName) like concat('%', lower(:search), '%')
                or lower(u.nickname) like concat('%', lower(:search), '%')
              )
        """)
    Page<UsersEntity> findAllByRoleAndSearchAndDelYn(
            @Param("role") String role,
            @Param("delYn") String delYn,
            @Param("search") String search,
            Pageable pageable);


}


