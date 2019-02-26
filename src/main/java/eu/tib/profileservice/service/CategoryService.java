package eu.tib.profileservice.service;

import java.util.List;

import eu.tib.profileservice.domain.Category;

public interface CategoryService {

	public List<Category> findAll();
	public List<Category> findByType(final Category.Type type);
	public Category findByTypeAndCategory(final Category.Type type, final String category);
	public Category save(final Category category);

}
