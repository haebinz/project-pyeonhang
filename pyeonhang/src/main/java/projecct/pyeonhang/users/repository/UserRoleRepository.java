    package projecct.pyeonhang.users.repository;

    import org.springframework.data.jpa.repository.JpaRepository;
    import projecct.pyeonhang.users.entity.UserRoleEntity;

    public interface UserRoleRepository extends JpaRepository<UserRoleEntity,String> {
    }
