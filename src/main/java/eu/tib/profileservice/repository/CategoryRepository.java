package eu.tib.profileservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.tib.profileservice.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	public List<Category> findByInstitution(final String institution);
}
