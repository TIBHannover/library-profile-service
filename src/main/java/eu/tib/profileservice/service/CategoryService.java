package eu.tib.profileservice.service;

import java.util.List;

import eu.tib.profileservice.domain.Category;

public interface CategoryService {

	public List<Category> findAll();
	public List<Category> findByInstitution(final String institution);	
	public Category save(final Category user);

}
