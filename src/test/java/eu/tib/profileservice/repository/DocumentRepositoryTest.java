package eu.tib.profileservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import eu.tib.profileservice.domain.Category;
import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.DocumentMetadata;
import eu.tib.profileservice.repository.DocumentRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
public class DocumentRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private DocumentRepository repository;

	private Category newCategory(final String institution, final String title) {
		Category category = new Category();
		category.setInstitution(institution);
		category.setCategory(title);
		return category;
	}

	private Document newDocument(final String author, final String title, final String description,
			final List<Category> categories) {
		final Document document = new Document();
		final DocumentMetadata documentMeta = new DocumentMetadata();
		documentMeta.setTitle(title);
		documentMeta.setAuthor(author);
		documentMeta.setDescription(description);
		document.setMetadata(documentMeta);
		document.setCategories(categories);
		return document;
	}

	@Test
	public void testDocumentRepository() {
		final Category category1 = newCategory("TEST1", "CAT1");
		final Category category2 = newCategory("TEST2", "CAT1");
		entityManager.persist(category1);
		entityManager.persist(category2);
		entityManager.persist(
				newDocument("testauthor", "testtitle", "testdesc", Arrays.asList(new Category[] { category1, category2 })));

		List<Document> documents = repository.findAll();
		assertThat(documents).isNotNull();
		assertThat(documents.size()).isEqualTo(1);
		final List<Category> categories = documents.get(0).getCategories();
		assertThat(categories).isNotNull();
		assertThat(categories.size()).isEqualTo(2);
	}
}
