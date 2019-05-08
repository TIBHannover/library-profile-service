package eu.tib.profileservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import eu.tib.profileservice.connector.InstitutionConnector;
import eu.tib.profileservice.connector.InstitutionConnectorFactory;
import eu.tib.profileservice.connector.InstitutionConnectorFactory.ConnectorType;
import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.DocumentMetadata;
import eu.tib.profileservice.repository.DocumentRepository;
import eu.tib.profileservice.util.FileExportProcessor;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
public class DocumentImportServiceIntegrationTest {

  @TestConfiguration
  static class TestContextConfiguration {
    @Bean
    public DocumentImportService service() {
      return new DocumentImportServiceImpl();
    }

    @Bean
    public DocumentService documentService() {
      return new DocumentServiceImpl();
    }
  }

  @Autowired
  private TestEntityManager entityManager;
  @Autowired
  private DocumentImportService documentImportService;
  @Autowired
  private DocumentRepository documentRepository;
  @MockBean
  private InstitutionConnectorFactory connectorFactory;
  @MockBean
  private InstitutionConnector connector;
  @MockBean
  private UserService userService;
  @MockBean
  private ImportFilterService importFilterService;
  @MockBean
  private FileExportProcessor fileExportProcessor;

  /**
   * Before.
   */
  @Before
  public void before() {
    entityManager.flush();
    entityManager.getEntityManager().createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE")
        .executeUpdate();
    entityManager.getEntityManager().createNativeQuery("TRUNCATE TABLE " + Document.ENTITY_NAME)
        .executeUpdate();
    entityManager.getEntityManager().createNativeQuery("TRUNCATE TABLE document_metadata");
    entityManager.getEntityManager().createNativeQuery("TRUNCATE TABLE document_metadata_ddcs");
    entityManager.getEntityManager().createNativeQuery("TRUNCATE TABLE document_metadata_isbns");
    entityManager.getEntityManager().createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE")
        .executeUpdate();
  }

  private DocumentMetadata newDocumentMetadataDummy(final String... isbns) {
    final DocumentMetadata document = new DocumentMetadata();
    document.setTitle("title");
    document.setRemainderOfTitle("remainderOfTitle");
    List<String> isbnList = new ArrayList<String>();
    for (String isbn : isbns) {
      isbnList.add(isbn);
    }
    document.setIsbns(isbnList);
    Set<String> categories = new HashSet<String>();
    categories.add("123.456");
    categories.add("987.6");
    document.setDeweyDecimalClassifications(categories);
    List<String> formKeywords = new ArrayList<String>();
    formKeywords.add("test");
    document.setFormKeywords(formKeywords);
    return document;
  }

  private Document newDocument(final DocumentMetadata documentMeta) {
    final Document document = new Document();
    document.setMetadata(documentMeta);
    document.setCreationDateUtc(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    document.setExpiryDateUtc(OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).toLocalDateTime());
    return document;
  }

  @Test
  public void importDocument() {
    Document existing1 = newDocument(newDocumentMetadataDummy("1234567890"));
    Document existing2 = newDocument(newDocumentMetadataDummy("0987654321"));
    entityManager.persist(existing1);
    entityManager.persist(existing2);
    entityManager.flush();

    DocumentMetadata documentMetadata = newDocumentMetadataDummy("1234567890", "0987654321");
    documentMetadata.setSource(ConnectorType.DNB.toString());
    when(connectorFactory.createConnector(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(
        new InstitutionConnector() {
          private boolean retrieved = false;

          @Override
          public List<DocumentMetadata> retrieveNextDocuments() {
            List<DocumentMetadata> result = new ArrayList<DocumentMetadata>();
            result.add(documentMetadata);
            retrieved = true;
            return result;
          }

          @Override
          public boolean hasNext() {
            return !retrieved;
          }

          @Override
          public boolean hasErrors() {
            return false;
          }
        });

    LocalDate dateTime = LocalDate.parse("2019-02-01");
    documentImportService.importDocuments(dateTime, dateTime, ConnectorType.DNB);

    List<Document> allDcouments = documentRepository.findAll();
    assertThat(allDcouments).isNotNull();
    assertThat(allDcouments.size()).isEqualTo(2);
    allDcouments.forEach(d -> assertThat(d.getMetadata().getIsbns().size()).isEqualTo(1));
  }

}
