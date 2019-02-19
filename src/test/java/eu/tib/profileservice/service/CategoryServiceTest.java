package eu.tib.profileservice.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import eu.tib.profileservice.domain.Category;
import eu.tib.profileservice.repository.CategoryRepository;
import eu.tib.profileservice.service.CategoryService;
import eu.tib.profileservice.service.CategoryServiceImpl;

@RunWith(SpringRunner.class)
public class CategoryServiceTest {
	
    @TestConfiguration
    static class CategoryServiceImplTestContextConfiguration {
  
        @Bean
        public CategoryService categoryService() {
            return new CategoryServiceImpl();
        }
    }

	@Autowired
	private CategoryService categoryService;
	@MockBean
	private CategoryRepository categoryRepository;

	private Category newCategory(final String institution, final String cat, final Long id) {
		Category category = new Category();
		category.setId(id);
		category.setInstitution(institution);
		category.setCategory(cat);
		return category;
	}

	@Test
	public void testFindByInstitution() {
		final List<Category> repositoryCategories = new ArrayList<Category>();
		repositoryCategories.add(newCategory("TEST1", "CAT1", 1L));
		repositoryCategories.add(newCategory("TEST1", "CAT2", 2L));
		Mockito.when(categoryRepository.findByInstitution("TEST1")).thenReturn(repositoryCategories);

		List<Category> categories = categoryService.findByInstitution("TEST1");
		assertThat(categories).isNotNull();
		assertThat(categories.size()).isEqualTo(2);
	}

	@Test
	public void testFindAll() {
		final List<Category> repositoryCategories = new ArrayList<Category>();
		repositoryCategories.add(newCategory("TEST1", "CAT1", 1L));
		repositoryCategories.add(newCategory("TEST1", "CAT2", 2L));
		repositoryCategories.add(newCategory("TEST2", "CAT1", 3L));
		Mockito.when(categoryRepository.findAll()).thenReturn(repositoryCategories);

		List<Category> categories = categoryService.findAll();
		assertThat(categories).isNotNull();
		assertThat(categories.size()).isEqualTo(3);
	}

	@Test
	public void testSave() {
		final Category repositoryCategory = newCategory("TEST1", "CAT1", 1L);
		Mockito.when(categoryRepository.save(Mockito.any(Category.class))).thenReturn(repositoryCategory);

		final Category category = newCategory("TEST1", "CAT1", null);
		final Category result = categoryService.save(category);
		assertThat(result).isNotNull();
	}
}
