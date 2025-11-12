package projecct.pyeonhang.email.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projecct.pyeonhang.email.entity.EmailEntity;
import projecct.pyeonhang.users.entity.UsersEntity;

import java.util.Optional;

public interface EmailRepository extends JpaRepository<EmailEntity,Long> {
    Optional<EmailEntity> findTopByUserAndEmailOrderByCreatedAtDesc(UsersEntity user, String email);
}
