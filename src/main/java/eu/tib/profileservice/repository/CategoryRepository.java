package eu.tib.profileservice.repository;

import eu.tib.profileservice.domain.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  List<Category> findByType(Category.Type type);

  Category findByTypeAndCategory(Category.Type type, String category);
}
