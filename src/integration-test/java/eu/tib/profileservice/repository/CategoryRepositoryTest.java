package eu.tib.profileservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import eu.tib.profileservice.domain.Category;
import eu.tib.profileservice.repository.CategoryRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
public class CategoryRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;
	@Autowired
	private CategoryRepository categoryRepository;

	private Category newCategory(final String institution, final String title) {
		Category category = new Category();
		category.setInstitution(institution);
		category.setCategory(title);
		return category;
	}

	@Test
	public void testCategoryRepository() {
		entityManager.persist(newCategory("TEST1", "CAT1"));
		entityManager.persist(newCategory("TEST1", "CAT2"));
		entityManager.persist(newCategory("TEST2", "CAT1"));

		List<Category> categories = categoryRepository.findByInstitution("TEST1");
		assertThat(categories).isNotNull();
		assertThat(categories.size()).isEqualTo(2);
		assertThat(categories).allMatch(c -> "TEST1".equals(c.getInstitution()));
		
		categories = categoryRepository.findByInstitution("TEST2");
		assertThat(categories).isNotNull();
		assertThat(categories.size()).isEqualTo(1);
		assertThat(categories).allMatch(c -> "TEST2".equals(c.getInstitution()));
	}
}
