package projecct.pyeonhang.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projecct.pyeonhang.category.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity,Integer> {
}
