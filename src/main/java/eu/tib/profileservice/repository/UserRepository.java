package eu.tib.profileservice.repository;

import eu.tib.profileservice.domain.Category;
import eu.tib.profileservice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  public User findByName(final String name);

  public User findByCategories(final Category category);

}
