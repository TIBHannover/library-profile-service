package eu.tib.profileservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import eu.tib.profileservice.domain.Category;
import eu.tib.profileservice.domain.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserServiceIntegrationTest {

  @TestConfiguration
  static class TestContextConfiguration {
    @Bean
    public UserService service() {
      return new UserServiceImpl();
    }
  }

  @Autowired
  private TestEntityManager entityManager;
  @Autowired
  private UserService userService;
  @MockBean
  private PasswordEncoder passwordEncoder;

  private Category newCategory(final Category.Type type, final String title) {
    Category category = new Category();
    category.setType(type);
    category.setCategory(title);
    return category;
  }

  private User newUser(final String name, final String password, final String initials,
      final List<Category> categories) {
    final User user = new User();
    user.setName(name);
    user.setPassword(password);
    user.setInitials(initials);
    user.setCategories(categories);
    return user;
  }

  /**
   * Before.
   */
  @Before
  public void before() {
    entityManager.flush();
    entityManager.getEntityManager().createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE")
        .executeUpdate();
    entityManager.getEntityManager().createNativeQuery("TRUNCATE TABLE " + Category.ENTITY_NAME)
        .executeUpdate();
    entityManager.getEntityManager().createNativeQuery("TRUNCATE TABLE " + User.ENTITY_NAME)
        .executeUpdate();
    entityManager.getEntityManager().createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE")
        .executeUpdate();

    when(passwordEncoder.encode(Mockito.anyString())).thenReturn("encryptedPw");
  }

  @Test
  public void createUserWithAlreadyAssignedCategories() {
    Category category1 = newCategory(Category.Type.DDC, "CAT1");
    category1 = entityManager.persist(category1);
    Category category2 = newCategory(Category.Type.DDC, "CAT2");
    category2 = entityManager.persist(category2);
    entityManager.persist(newUser("name", "pw", "np", new ArrayList<Category>(Arrays
        .asList(category1))));

    User user = newUser("name2", "pw", "ti", new ArrayList<Category>(Arrays
        .asList(category1, category2)));

    userService.create(user);

    User result = userService.findByName("name2");
    assertThat(result).isNotNull();
    assertThat(result.getCategories().size()).isEqualTo(2);
    result = userService.findByName("name");
    assertThat(result).isNotNull();
    assertThat(result.getCategories().size()).isEqualTo(0);
  }

  @Test
  public void updateUserWithAlreadyAssignedCategories() {
    Category category1 = newCategory(Category.Type.DDC, "CAT1");
    category1 = entityManager.persist(category1);
    Category category2 = newCategory(Category.Type.DDC, "CAT2");
    category2 = entityManager.persist(category2);
    Category category3 = newCategory(Category.Type.DDC, "CAT3");
    category3 = entityManager.persist(category3);
    entityManager.persist(newUser("name", "pw", "np", new ArrayList<Category>(Arrays
        .asList(category1, category3))));
    Long userId = entityManager.persist(newUser("name2", "pw", "ti", new ArrayList<Category>(Arrays
        .asList(category2)))).getId();

    User user = newUser("name2", "pw", "ti", new ArrayList<Category>(Arrays
        .asList(category3, category2)));
    user.setId(userId);

    userService.update(user);

    User result = userService.findByName("name2");
    assertThat(result).isNotNull();
    assertThat(result.getCategories().size()).isEqualTo(2);
    result = userService.findByName("name");
    assertThat(result).isNotNull();
    assertThat(result.getCategories().size()).isEqualTo(1);
  }

}
