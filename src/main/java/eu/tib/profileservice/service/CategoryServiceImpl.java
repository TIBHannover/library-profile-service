package eu.tib.profileservice.service;

import eu.tib.profileservice.domain.Category;
import eu.tib.profileservice.repository.CategoryRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {

  @Autowired
  private CategoryRepository categoryRepository;

  @Transactional(readOnly = true)
  @Override
  public List<Category> findAll() {
    return categoryRepository.findAll();
  }

  @Transactional(readOnly = true)
  @Override
  public List<Category> findByType(final Category.Type type) {
    return categoryRepository.findByType(type);
  }

  @Transactional
  @Override
  public Category save(Category category) {
    return categoryRepository.save(category);
  }

  @Transactional(readOnly = true)
  @Override
  public Category findByTypeAndCategory(final Category.Type type, String category) {
    return categoryRepository.findByTypeAndCategory(type, category);
  }

}
