    package projecct.pyeonhang.coupon.repository;

    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.jpa.repository.JpaRepository;
    import projecct.pyeonhang.coupon.entity.CouponEntity;

    public interface CouponRepository extends JpaRepository<CouponEntity,Integer> {

        @Override
        Page<CouponEntity> findAll(Pageable pageable);
    }
