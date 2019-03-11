package eu.tib.profileservice.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.tib.profileservice.connector.ConnectorException;
import eu.tib.profileservice.connector.InstitutionConnector;
import eu.tib.profileservice.connector.InstitutionConnectorFactory;
import eu.tib.profileservice.connector.InstitutionConnectorFactory.ConnectorType;
import eu.tib.profileservice.connector.InventoryConnector;
import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.DocumentMetadata;
import eu.tib.profileservice.repository.DocumentRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class DocumentImportServiceTest {

  @TestConfiguration
  static class TestContextConfiguration {

    @Bean
    public DocumentImportService service() {
      return new DocumentImportServiceImpl();
    }
  }

  @Autowired
  private DocumentImportService documentImportService;
  @MockBean
  private DocumentRepository documentRepository;
  @MockBean
  private InstitutionConnectorFactory connectorFactory;
  @MockBean
  private InstitutionConnector connector;
  @MockBean
  private InventoryConnector inventoryConnector;
  @MockBean
  private UserService userService;

  private DocumentMetadata newDocumentMetadataDummy() {
    final DocumentMetadata document = new DocumentMetadata();
    document.setTitle("title");
    document.setRemainderOfTitle("remainderOfTitle");
    document.setIsbns(Arrays.asList(new String[] {"1234567890"}));
    return document;
  }

  /**
   * Setup.
   */
  @Before
  public void setup() {
    when(connectorFactory.createConnector(Mockito.any(ConnectorType.class), Mockito.any(
            LocalDate.class), Mockito.any(LocalDate.class)))
                .thenReturn(connector);
    when(connector.hasNext()).thenReturn(true).thenReturn(false);
  }

  @Test
  public void testImportDocumentsWithInvalidResult() {
    DocumentMetadata invalidMetadata = newDocumentMetadataDummy();
    invalidMetadata.setIsbns(null);
    List<DocumentMetadata> connectorResult = Arrays.asList(new DocumentMetadata[] {
        invalidMetadata});
    when(connector.retrieveNextDocuments()).thenReturn(connectorResult);
    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    documentImportService.importDocuments(now, now);

    verify(documentRepository, times(0)).save(Mockito.any(Document.class));
  }

  @Test
  public void testImportDocumentsWithNullResult() {
    when(connector.retrieveNextDocuments()).thenReturn(null);
    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    documentImportService.importDocuments(now, now);

    verify(documentRepository, times(0)).save(Mockito.any(Document.class));
  }

  @Test
  public void testImportDocumentsWithoutExistingDocument() {
    List<DocumentMetadata> connectorResult = Arrays.asList(new DocumentMetadata[] {
        newDocumentMetadataDummy()});
    when(connector.retrieveNextDocuments()).thenReturn(connectorResult);
    when(documentRepository.findByMetadataIsbns(Mockito.anyString())).thenReturn(null);

    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    documentImportService.importDocuments(now, now);

    verify(documentRepository, times(1)).save(Mockito.any(Document.class));
  }

  @Test
  public void testImportDocumentsWithExistingLocalDocument() {
    Document existingDocument = new Document();
    existingDocument.setMetadata(newDocumentMetadataDummy());
    List<DocumentMetadata> connectorResult = Arrays.asList(new DocumentMetadata[] {
        newDocumentMetadataDummy()});
    when(connector.retrieveNextDocuments()).thenReturn(connectorResult);
    when(documentRepository.findByMetadataIsbns(Mockito.anyString())).thenReturn(existingDocument);

    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    documentImportService.importDocuments(now, now);

    verify(documentRepository, times(0)).save(Mockito.any(Document.class));
  }

  @Test
  public void testImportDocumentsWithExistingRemoteDocument() throws ConnectorException {
    Document existingDocument = new Document();
    existingDocument.setMetadata(newDocumentMetadataDummy());
    List<DocumentMetadata> connectorResult = Arrays.asList(new DocumentMetadata[] {
        newDocumentMetadataDummy()});
    when(connector.retrieveNextDocuments()).thenReturn(connectorResult);
    when(documentRepository.findByMetadataIsbns(Mockito.anyString())).thenReturn(null);
    when(inventoryConnector.contains(Mockito.any(DocumentMetadata.class))).thenReturn(true);

    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    documentImportService.importDocuments(now, now);

    verify(documentRepository, times(0)).save(Mockito.any(Document.class));
  }

  @Test
  public void testImportDocumentsWithExistingRemoteConnectorFailure() throws ConnectorException {
    Document existingDocument = new Document();
    existingDocument.setMetadata(newDocumentMetadataDummy());
    List<DocumentMetadata> connectorResult = Arrays.asList(new DocumentMetadata[] {
        newDocumentMetadataDummy()});
    when(connector.retrieveNextDocuments()).thenReturn(connectorResult);
    when(documentRepository.findByMetadataIsbns(Mockito.anyString())).thenReturn(null);
    when(inventoryConnector.contains(Mockito.any(DocumentMetadata.class))).thenThrow(
        ConnectorException.class);

    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    documentImportService.importDocuments(now, now);

    verify(documentRepository, times(1)).save(Mockito.any(Document.class));
  }

}
