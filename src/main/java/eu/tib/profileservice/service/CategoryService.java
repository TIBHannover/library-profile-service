package eu.tib.profileservice.service;

import eu.tib.profileservice.domain.Category;
import java.util.List;

public interface CategoryService {

  public List<Category> findAll();

  public List<Category> findByType(final Category.Type type);

  public Category findByTypeAndCategory(final Category.Type type, final String category);

  public Category save(final Category category);

}
