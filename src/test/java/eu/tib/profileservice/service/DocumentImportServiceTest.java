package eu.tib.profileservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.tib.profileservice.connector.ConnectorException;
import eu.tib.profileservice.connector.InstitutionConnector;
import eu.tib.profileservice.connector.InstitutionConnectorFactory;
import eu.tib.profileservice.connector.InstitutionConnectorFactory.ConnectorType;
import eu.tib.profileservice.connector.InventoryConnector;
import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.Document.Status;
import eu.tib.profileservice.domain.DocumentMetadata;
import eu.tib.profileservice.domain.ImportFilter;
import eu.tib.profileservice.domain.ImportFilter.Action;
import eu.tib.profileservice.domain.ImportFilter.ConditionType;
import eu.tib.profileservice.repository.DocumentImportStatisticsRepository;
import eu.tib.profileservice.repository.DocumentRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = DocumentImportServiceImpl.class)
@TestPropertySource(value = "classpath:application.properties")
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
  private DocumentImportStatisticsRepository documentImportStatisticsRepository;
  @MockBean
  private InstitutionConnectorFactory connectorFactory;
  @MockBean
  private InstitutionConnector connector;
  @MockBean
  private UserService userService;
  @MockBean
  private DocumentService documentService;
  @MockBean
  private ImportFilterService importFilterService;

  private DocumentMetadata newDocumentMetadataDummy() {
    final DocumentMetadata document = new DocumentMetadata();
    document.setTitle("title");
    document.setRemainderOfTitle("remainderOfTitle");
    document.setIsbns(Arrays.asList(new String[] {"1234567890"}));
    Set<String> categories = new HashSet<String>();
    categories.add("123.456");
    categories.add("987.6");
    document.setDeweyDecimalClassifications(categories);
    List<String> formKeywords = new ArrayList<String>();
    formKeywords.add("test");
    document.setFormKeywords(formKeywords);
    return document;
  }

  private ImportFilter newIgnoreFilter(final String condition, final ConditionType conditionType) {
    ImportFilter filter = new ImportFilter();
    filter.setAction(Action.IGNORE);
    filter.setCondition(condition);
    filter.setConditionType(conditionType);
    return filter;
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
    documentImportService.importDocuments(now, now, ConnectorType.DNB);

    verify(documentService, times(0)).saveDocument(Mockito.any(Document.class));
  }

  @Test
  public void testImportDocumentsWithNullResult() {
    when(connector.retrieveNextDocuments()).thenReturn(null);
    when(connector.hasErrors()).thenReturn(true);
    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    documentImportService.importDocuments(now, now, ConnectorType.DNB);

    verify(documentService, times(0)).saveDocument(Mockito.any(Document.class));
  }

  @Test
  public void testImportDocumentsWithMatchingFilter() {
    List<ImportFilter> filters = Arrays.asList(new ImportFilter[] {newIgnoreFilter("test",
        ConditionType.FORM_KEYWORD)});
    List<DocumentMetadata> connectorResult = Arrays.asList(new DocumentMetadata[] {
        newDocumentMetadataDummy()});
    when(importFilterService.findAll()).thenReturn(filters);
    when(connector.retrieveNextDocuments()).thenReturn(connectorResult);
    when(documentRepository.findByMetadataIsbnsContains(Mockito.anyString())).thenReturn(null);

    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    documentImportService.importDocuments(now, now, ConnectorType.DNB);

    ArgumentCaptor<Document> arg1 = ArgumentCaptor.forClass(Document.class);
    verify(documentService, times(1)).saveDocument(arg1.capture());
    Document doc = arg1.getValue();
    assertThat(doc).isNotNull();
    assertThat(doc.getStatus()).isEqualTo(Status.IGNORED);
  }

  @Test
  public void testImportDocumentsWithoutExistingDocument() {
    List<DocumentMetadata> connectorResult = Arrays.asList(new DocumentMetadata[] {
        newDocumentMetadataDummy()});
    when(connector.retrieveNextDocuments()).thenReturn(connectorResult);
    when(documentRepository.findByMetadataIsbnsContains(Mockito.anyString())).thenReturn(null);

    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    documentImportService.importDocuments(now, now, ConnectorType.DNB);

    verify(documentService, times(1)).saveDocument(Mockito.any(Document.class));
  }

  @Test
  public void testImportDocumentsWithExistingLocalDocument() {
    Document existingDocument = new Document();
    existingDocument.setMetadata(newDocumentMetadataDummy());
    List<DocumentMetadata> connectorResult = Arrays.asList(new DocumentMetadata[] {
        newDocumentMetadataDummy()});
    when(connector.retrieveNextDocuments()).thenReturn(connectorResult);
    when(documentRepository.findByMetadataIsbnsContains(Mockito.anyString())).thenReturn(
        existingDocument);

    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    documentImportService.importDocuments(now, now, ConnectorType.DNB);

    verify(documentService, times(0)).saveDocument(Mockito.any(Document.class));
  }

  @Test
  public void testImportDocumentsWithExistingLocalDocumentHigherPriority() {
    Document existingDocument = new Document();
    existingDocument.setMetadata(newDocumentMetadataDummy());
    existingDocument.getMetadata().setSource(ConnectorType.BL.toString());
    List<DocumentMetadata> connectorResult = Arrays.asList(new DocumentMetadata[] {
        newDocumentMetadataDummy()});
    connectorResult.get(0).setSource(ConnectorType.DNB.toString());
    when(connector.retrieveNextDocuments()).thenReturn(connectorResult);
    when(documentRepository.findByMetadataIsbnsContains(Mockito.anyString())).thenReturn(
        existingDocument);

    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    documentImportService.importDocuments(now, now, ConnectorType.DNB);

    verify(documentService, times(1)).saveDocument(Mockito.any(Document.class));
  }

  @Test
  public void testImportDocumentsWithExistingLocalDocumentLowerPriority() {
    Document existingDocument = new Document();
    existingDocument.setMetadata(newDocumentMetadataDummy());
    existingDocument.getMetadata().setSource(ConnectorType.DNB.toString());
    List<DocumentMetadata> connectorResult = Arrays.asList(new DocumentMetadata[] {
        newDocumentMetadataDummy()});
    connectorResult.get(0).setSource(ConnectorType.BL.toString());
    when(connector.retrieveNextDocuments()).thenReturn(connectorResult);
    when(documentRepository.findByMetadataIsbnsContains(Mockito.anyString())).thenReturn(
        existingDocument);

    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    documentImportService.importDocuments(now, now, ConnectorType.BL);

    verify(documentService, times(0)).saveDocument(Mockito.any(Document.class));
  }

  @Test
  public void testImportDocumentsWithExistingRemoteDocument() throws ConnectorException {
    InventoryConnector inventoryConnector = Mockito.mock(InventoryConnector.class);
    ((DocumentImportServiceImpl) documentImportService).setInventoryConnector(inventoryConnector);

    Document existingDocument = new Document();
    existingDocument.setMetadata(newDocumentMetadataDummy());
    List<DocumentMetadata> connectorResult = Arrays.asList(new DocumentMetadata[] {
        newDocumentMetadataDummy()});
    when(connector.retrieveNextDocuments()).thenReturn(connectorResult);
    when(documentRepository.findByMetadataIsbnsContains(Mockito.anyString())).thenReturn(null);
    when(inventoryConnector.contains(Mockito.any(DocumentMetadata.class))).thenReturn(true);

    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    documentImportService.importDocuments(now, now, ConnectorType.DNB);

    ArgumentCaptor<Document> arg1 = ArgumentCaptor.forClass(Document.class);
    verify(documentService, times(1)).saveDocument(arg1.capture());
    Document doc = arg1.getValue();
    assertThat(doc).isNotNull();
    assertThat(doc.getMetadata()).isNotNull();
    assertTrue(doc.getMetadata().isContainedInInventory());

    ((DocumentImportServiceImpl) documentImportService).setInventoryConnector(null);
  }

  @Test
  public void testImportDocumentsWithoutExistingRemoteDocument() throws ConnectorException {
    InventoryConnector inventoryConnector = Mockito.mock(InventoryConnector.class);
    ((DocumentImportServiceImpl) documentImportService).setInventoryConnector(inventoryConnector);

    Document existingDocument = new Document();
    existingDocument.setMetadata(newDocumentMetadataDummy());
    List<DocumentMetadata> connectorResult = Arrays.asList(new DocumentMetadata[] {
        newDocumentMetadataDummy()});
    when(connector.retrieveNextDocuments()).thenReturn(connectorResult);
    when(documentRepository.findByMetadataIsbnsContains(Mockito.anyString())).thenReturn(null);
    when(inventoryConnector.contains(Mockito.any(DocumentMetadata.class))).thenReturn(false);

    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    documentImportService.importDocuments(now, now, ConnectorType.DNB);

    ArgumentCaptor<Document> arg1 = ArgumentCaptor.forClass(Document.class);
    verify(documentService, times(1)).saveDocument(arg1.capture());
    Document doc = arg1.getValue();
    assertThat(doc).isNotNull();
    assertThat(doc.getMetadata()).isNotNull();
    assertFalse(doc.getMetadata().isContainedInInventory());
    ((DocumentImportServiceImpl) documentImportService).setInventoryConnector(null);
  }

  @Test
  public void testImportDocumentsWithExistingRemoteConnectorFailure() throws ConnectorException {
    InventoryConnector inventoryConnector = Mockito.mock(InventoryConnector.class);
    ((DocumentImportServiceImpl) documentImportService).setInventoryConnector(inventoryConnector);
    Document existingDocument = new Document();
    existingDocument.setMetadata(newDocumentMetadataDummy());
    List<DocumentMetadata> connectorResult = Arrays.asList(new DocumentMetadata[] {
        newDocumentMetadataDummy()});
    when(connector.retrieveNextDocuments()).thenReturn(connectorResult);
    when(documentRepository.findByMetadataIsbnsContains(Mockito.anyString())).thenReturn(null);
    when(inventoryConnector.contains(Mockito.any(DocumentMetadata.class))).thenThrow(
        ConnectorException.class);

    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    documentImportService.importDocuments(now, now, ConnectorType.DNB);

    ArgumentCaptor<Document> arg1 = ArgumentCaptor.forClass(Document.class);
    verify(documentService, times(1)).saveDocument(arg1.capture());
    Document doc = arg1.getValue();
    assertThat(doc).isNotNull();
    assertThat(doc.getMetadata()).isNotNull();
    assertThat(doc.getMetadata().isContainedInInventory()).isNull();
    ;
    ((DocumentImportServiceImpl) documentImportService).setInventoryConnector(null);
  }

}
