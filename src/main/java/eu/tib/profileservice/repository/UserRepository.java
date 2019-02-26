package eu.tib.profileservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.tib.profileservice.domain.Category;
import eu.tib.profileservice.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	public User findByName(final String name);
	public User findByCategories(final Category category);

}
