package eu.tib.profileservice.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.junit4.SpringRunner;

import eu.tib.profileservice.domain.Category;
import eu.tib.profileservice.domain.User;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CategoryRepository categoryRepository;

	private Category newCategory(final Category.Type type, final String title) {
		Category category = new Category();
		category.setType(type);
		category.setCategory(title);
		return category;
	}
	private User newUser(final String name, final String password, final String initials, final List<Category> categories) {
		final User user = new User();
		user.setName(name);
		user.setPassword(password);
		user.setInitials(initials);
		user.setCategories(categories);
		return user;
	}

	@Test
	public void testUserRepository() {
		Category category1 = newCategory(Category.Type.DDC, "CAT1");
		category1 = entityManager.persist(category1);
		Category category2 = newCategory(Category.Type.DDC, "CAT2");
		category2 = entityManager.persist(category2);
		Category category3 = newCategory(Category.Type.DDC, "CAT3");
		category3 = entityManager.persist(category3);
		entityManager.persist(newUser("name", "pw", "np", Arrays.asList(new Category[] {category1, category3})));
		entityManager.persist(newUser("name2", "pw", "np2", Arrays.asList(new Category[] {category2})));

		User user = userRepository.findByCategories(category1);
		assertThat(user).isNotNull();
		assertThat(user.getName()).isEqualTo("name");
		assertThat(user.getCategories()).isNotNull();
		assertThat(user.getCategories().size()).isEqualTo(2);
	}
	
	/**
	 * Test user creation with category that is already assigned
	 */
	@Test(expected=DataAccessException.class)
	public void testCreateUserWithAlreadyAssignedCategory() {
		Category category1 = newCategory(Category.Type.DDC, "CAT1");
		category1 = entityManager.persist(category1);
		userRepository.save(newUser("name1", "pw", "np1", Arrays.asList(new Category[] {category1})));
		userRepository.save(newUser("name2", "pw", "np2", Arrays.asList(new Category[] {category1})));
		userRepository.flush();
	}
	
	@Test
	public void testDeleteUser() {
		Category category1 = newCategory(Category.Type.DDC, "CAT1");
		category1 = entityManager.persist(category1);
		User user = entityManager.persist(newUser("name", "pw", "np", Arrays.asList(new Category[] {category1})));
		userRepository.delete(user);
		userRepository.flush();
		Optional<Category> result = categoryRepository.findById(category1.getId());
		assertTrue("category was deleted", result.isPresent());
	}

}
