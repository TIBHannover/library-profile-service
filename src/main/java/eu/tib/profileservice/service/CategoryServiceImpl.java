package eu.tib.profileservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.tib.profileservice.domain.Category;
import eu.tib.profileservice.repository.CategoryRepository;

@Service
public class CategoryServiceImpl implements CategoryService {
	
	@Autowired
	private CategoryRepository categoryRepository;

	@Override
	public List<Category> findAll() {
		return categoryRepository.findAll();
	}

	@Transactional
	@Override
	public List<Category> findByInstitution(String institution) {
		return categoryRepository.findByInstitution(institution);
	}

	@Transactional
	@Override
	public Category save(Category category) {
		return categoryRepository.save(category);
	}

}
