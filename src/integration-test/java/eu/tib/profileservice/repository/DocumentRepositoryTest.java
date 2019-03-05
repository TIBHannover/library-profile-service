package eu.tib.profileservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import eu.tib.profileservice.domain.Category;
import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.DocumentMetadata;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
public class DocumentRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private DocumentRepository repository;

  private Category newCategory(final Category.Type type, final String title) {
    Category category = new Category();
    category.setType(type);
    category.setCategory(title);
    return category;
  }

  private Document newDocument(final String author, final String title, final String description,
      final Set<String> ddcCategories) {
    final Document document = new Document();
    final DocumentMetadata documentMeta = new DocumentMetadata();
    documentMeta.setTitle(title);
    documentMeta.setAuthor(author);
    documentMeta.setDescription(description);
    documentMeta.setDeweyDecimalClassifications(ddcCategories);
    document.setMetadata(documentMeta);
    document.setCreationDateUtc(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    return document;
  }

  @Test
  public void testDocumentRepository() {
    final Category category1 = newCategory(Category.Type.DDC, "CAT1");
    final Category category2 = newCategory(Category.Type.DDC, "CAT2");
    entityManager.persist(category1);
    entityManager.persist(category2);
    entityManager.persist(
        newDocument("testauthor", "testtitle", "testdesc", new HashSet<String>(Arrays.asList(
            new String[] {"104", "345.678"}))));

    List<Document> documents = repository.findAll();
    assertThat(documents).isNotNull();
    assertThat(documents.size()).isEqualTo(1);
    final Set<String> deweyDecimalClassifications = documents.get(0).getMetadata()
        .getDeweyDecimalClassifications();
    assertThat(deweyDecimalClassifications).isNotNull();
    assertThat(deweyDecimalClassifications.size()).isEqualTo(2);
  }
}
