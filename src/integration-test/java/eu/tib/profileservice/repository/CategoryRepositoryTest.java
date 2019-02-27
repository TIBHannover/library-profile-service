package eu.tib.profileservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
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

	private Category newCategory(final Category.Type type, final String title) {
		Category category = new Category();
		category.setType(type);
		category.setCategory(title);
		return category;
	}
	
	@Before
	public void before() {
		entityManager.flush();
        entityManager.getEntityManager().createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
		entityManager.getEntityManager().createNativeQuery("TRUNCATE TABLE " + Category.ENTITY_NAME).executeUpdate();
        entityManager.getEntityManager().createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
	}

	@Test
	public void testCategoryRepository() {
		entityManager.persist(newCategory(Category.Type.DDC, "CAT1"));
		entityManager.persist(newCategory(Category.Type.DDC, "CAT2"));

		List<Category> categories = categoryRepository.findByType(Category.Type.DDC);
		assertThat(categories).isNotNull();
		assertThat(categories.size()).isEqualTo(2);
		assertThat(categories).allMatch(c -> Category.Type.DDC.equals(c.getType()));
	}
}
