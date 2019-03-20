package eu.tib.profileservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import eu.tib.profileservice.domain.Category;
import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.DocumentMetadata;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
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

  private Document newDocument(final String title, final Set<String> ddcCategories) {
    final Document document = new Document();
    final DocumentMetadata documentMeta = new DocumentMetadata();
    documentMeta.setTitle(title);
    documentMeta.setDeweyDecimalClassifications(ddcCategories);
    document.setMetadata(documentMeta);
    document.setCreationDateUtc(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    return document;
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
    entityManager.getEntityManager().createNativeQuery("TRUNCATE TABLE " + Document.ENTITY_NAME)
        .executeUpdate();
    entityManager.getEntityManager().createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE")
        .executeUpdate();
  }

  @Test
  public void testDocumentRepository() {
    final Category category1 = newCategory(Category.Type.DDC, "CAT1");
    final Category category2 = newCategory(Category.Type.DDC, "CAT2");
    entityManager.persist(category1);
    entityManager.persist(category2);
    entityManager.persist(
        newDocument("testtitle", new HashSet<String>(Arrays.asList(new String[] {"104",
            "345.678"}))));

    List<Document> documents = repository.findAll();
    assertThat(documents).isNotNull();
    assertThat(documents.size()).isEqualTo(1);
    final Set<String> deweyDecimalClassifications = documents.get(0).getMetadata()
        .getDeweyDecimalClassifications();
    assertThat(deweyDecimalClassifications).isNotNull();
    assertThat(deweyDecimalClassifications.size()).isEqualTo(2);
  }

  @Test
  public void testDeleteByCreationDateUtcBefore() {
    Document document1 = newDocument("title1", new HashSet<String>(Arrays.asList(new String[] {
        "300"})));
    document1.setCreationDateUtc(LocalDateTime.parse("2019-01-01T10:00:00"));
    Document document2 = newDocument("title2", new HashSet<String>(Arrays.asList(new String[] {
        "300"})));
    document2.setCreationDateUtc(LocalDateTime.parse("2019-04-01T10:00:00"));
    entityManager.persist(document1);
    entityManager.persist(document2);

    LocalDateTime expiryDate = LocalDateTime.parse("2019-02-01T00:00:00");
    repository.deleteByCreationDateUtcBefore(expiryDate);
    List<Document> documents = repository.findAll();
    assertThat(documents).isNotNull();
    assertThat(documents.size()).isEqualTo(1);
  }
}
