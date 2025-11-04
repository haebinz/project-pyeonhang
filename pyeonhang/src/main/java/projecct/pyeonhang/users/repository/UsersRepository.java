package projecct.pyeonhang.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}


