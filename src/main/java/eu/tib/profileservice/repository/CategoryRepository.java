package eu.tib.profileservice.repository;

import eu.tib.profileservice.domain.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  public List<Category> findByType(final Category.Type type);

  public Category findByTypeAndCategory(final Category.Type type, final String category);
}
