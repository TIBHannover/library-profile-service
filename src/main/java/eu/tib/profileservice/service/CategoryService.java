package eu.tib.profileservice.service;

import eu.tib.profileservice.domain.Category;
import java.util.List;

public interface CategoryService {

  List<Category> findAll();

  List<Category> findByType(final Category.Type type);

  Category findByTypeAndCategory(final Category.Type type, final String category);

  Category save(final Category category);

}
