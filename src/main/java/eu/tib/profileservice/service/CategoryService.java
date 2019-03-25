package eu.tib.profileservice.service;

import eu.tib.profileservice.domain.Category;
import java.util.List;

public interface CategoryService {

  List<Category> findAll();

  List<Category> findByType(Category.Type type);

  Category findByTypeAndCategory(Category.Type type, String category);

  Category save(Category category);

}
