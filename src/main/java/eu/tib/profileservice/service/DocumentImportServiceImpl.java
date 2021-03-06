package eu.tib.profileservice.service;

import eu.tib.profileservice.connector.ConnectorException;
import eu.tib.profileservice.connector.InstitutionConnector;
import eu.tib.profileservice.connector.InstitutionConnectorFactory;
import eu.tib.profileservice.connector.InstitutionConnectorFactory.ConnectorType;
import eu.tib.profileservice.connector.InventoryConnector;
import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.Document.Status;
import eu.tib.profileservice.domain.DocumentImportStatistics;
import eu.tib.profileservice.domain.DocumentMetadata;
import eu.tib.profileservice.domain.ImportFilter;
import eu.tib.profileservice.repository.DocumentImportStatisticsRepository;
import eu.tib.profileservice.util.DocumentAssignmentFinder;
import eu.tib.profileservice.util.DocumentSourceComparator;
import eu.tib.profileservice.util.ImportFilterProcessor;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PropertySource(value = "file:${envConfigDir:envConf/default/}profileservice.properties")
public class DocumentImportServiceImpl implements DocumentImportService {

  private static final Logger LOG = LoggerFactory.getLogger(DocumentImportServiceImpl.class);

  /** Cleanup documents with creation date older than this amount of months. */
  @Value("${document.expiry.months.default}")
  private int defaultExpiryMonths;

  @Value("${document.source.priorities}")
  private String sourcePriorityList;

  @Autowired
  private InstitutionConnectorFactory connectorFactory;

  @Autowired(required = false)
  private InventoryConnector inventoryConnector;

  @Autowired
  private UserService userService;

  @Autowired
  private DocumentService documentService;

  @Autowired
  private DocumentImportStatisticsRepository importStatisticsRepository;

  @Autowired
  private ImportFilterService importFilterService;

  @Transactional
  @Override
  public void importDocuments(final LocalDate from, final LocalDate to,
      final ConnectorType connectorType) {
    LOG.info("Import documents from {} (from: {}, to: {})", connectorType, from, to);
    DocumentImportStatistics statistics = initStatistics(from, to, connectorType);
    if (inventoryConnector == null) {
      LOG.debug("no inventory connector");
    }
    List<ImportFilter> filterRules = importFilterService.findAll();
    ImportFilterProcessor filterProcessor = new ImportFilterProcessor(filterRules);
    DocumentAssignmentFinder documentAssignmentFinder = new DocumentAssignmentFinder(userService
        .findAll());

    InstitutionConnector connector = connectorFactory.createConnector(connectorType, from, to);
    while (connector.hasNext()) {
      LOG.debug("going for next request (retrieved {} documents yet)", statistics.getNrRetrieved());
      List<DocumentMetadata> documents = connector.retrieveNextDocuments();
      if (documents == null) {
        LOG.error("Cannot retrieve documents from {}", connectorType);
        break;
      } else {
        statistics.addNrRetrieved(documents.size());
        documents.forEach(doc -> createNewDocument(doc, documentAssignmentFinder, filterProcessor,
            statistics));
      }
    }
    if (connector.hasErrors()) {
      LOG.error("There was an error while retrieving documents from {}", connectorType);
      statistics.setErrorInInstitutionConnector(true);
    }
    finishStatistics(statistics);
  }

  /**
   * Persist a new {@link Document} for the given {@link DocumentMetadata}, if not already existing.
   */
  private void createNewDocument(final DocumentMetadata documentMetadata,
      final DocumentAssignmentFinder documentAssignmentFinder,
      final ImportFilterProcessor filterProcessor, final DocumentImportStatistics statistics) {
    if (!isValid(documentMetadata)) {
      LOG.debug("invalid document: {}", buildDocumentMetadataString(documentMetadata));
      statistics.addNrInvalid(1);
      return;
    }
    try {
      processInventoryCheck(documentMetadata);
    } catch (ConnectorException e) {
      LOG.error("error in inventory connector while checking " + buildDocumentMetadataString(
          documentMetadata), e);
      statistics.setErrorInInventoryConnector(true);
    }
    List<Document> existingDocuments = getExistingDocuments(documentMetadata);
    if (existingDocuments.size() > 0) {
      LOG.debug("document already exists in local inventory: {}", buildDocumentMetadataString(
          documentMetadata));
      statistics.addNrExists(1);
      if (existingDocuments.size() > 1) {
        LOG.debug("more than one document exists, no update!");
      } else {
        Document existingDocument = existingDocuments.get(0);
        Comparator<DocumentMetadata> comparator = new DocumentSourceComparator(sourcePriorityList);
        if (comparator.compare(documentMetadata, existingDocument.getMetadata()) > 0) {
          updateExistingDocument(existingDocument, documentMetadata);
          statistics.addNrUpdated(1);
        }
      }
    } else {
      Document document = new Document();
      OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
      document.setCreationDateUtc(utc.toLocalDateTime());
      document.setExpiryDateUtc(utc.plusMonths(defaultExpiryMonths).toLocalDateTime());
      document.setMetadata(documentMetadata);

      filterProcessor.process(document);

      if (Status.IGNORED.equals(document.getStatus())) {
        statistics.addNrIgnored(1);
      } else { // currently only IGNORED is possible for FilterProcessor
        document.setStatus(Status.IN_PROGRESS);
        document.setAssignee(documentAssignmentFinder.determineAssignee(documentMetadata));
        statistics.addNrImported(1);
      }
      documentService.saveDocument(document);
      //LOG.debug("document imported: {}", buildDocumentMetadataString(documentMetadata));
    }
  }

  private void updateExistingDocument(final Document existingDocument,
      final DocumentMetadata newData) {
    LOG.debug("update existing document: {}", buildDocumentMetadataString(existingDocument
        .getMetadata()));
    DocumentMetadata existingData = existingDocument.getMetadata();
    existingData.setAuthors(newData.getAuthors());
    existingData.setBibliographyNumbers(newData.getBibliographyNumbers());
    existingData.setContainedInInventory(newData.isContainedInInventory());
    existingData.setDateOfPublication(newData.getDateOfPublication());
    existingData.setDeweyDecimalClassifications(newData.getDeweyDecimalClassifications());
    existingData.setEdition(newData.getEdition());
    existingData.setFormKeywords(newData.getFormKeywords());
    existingData.setFormOfProduct(newData.getFormOfProduct());
    existingData.setIsbns(newData.getIsbns());
    existingData.setPhysicalDescription(newData.getPhysicalDescription());
    existingData.setPlaceOfPublication(newData.getPlaceOfPublication());
    existingData.setPublisher(newData.getPublisher());
    existingData.setRemainderOfTitle(newData.getRemainderOfTitle());
    existingData.setSeries(newData.getSeries());
    existingData.setSource(newData.getSource());
    existingData.setTermsOfAvailability(newData.getTermsOfAvailability());
    existingData.setTitle(newData.getTitle());
    existingData.setInventoryUris(newData.getInventoryUris());
    documentService.saveDocument(existingDocument);
  }

  private void processInventoryCheck(final DocumentMetadata documentMetadata)
      throws ConnectorException {
    if (inventoryConnector != null) {
      boolean exists = inventoryConnector.processInventoryCheck(documentMetadata);
      documentMetadata.setContainedInInventory(exists);
    }
  }

  /**
   * Check if the given {@link DocumentMetadata} already exists in the inventory (local).
   *
   * @param documentMetadata has to match this document
   * @return the document, if it is contained in the inventory; null, otherwise
   */
  private List<Document> getExistingDocuments(final DocumentMetadata documentMetadata) {
    // check local inventory
    List<Document> result = new ArrayList<Document>();
    Set<Long> ids = new HashSet<Long>();
    for (String isbn : documentMetadata.getIsbns()) {
      Document existingDocument = documentService.findByMetadataIsbnsContains(isbn);
      if (existingDocument != null && !ids.contains(existingDocument.getId())) {
        result.add(existingDocument);
        ids.add(existingDocument.getId());
      }
    }
    return result;
  }

  private String buildDocumentMetadataString(final DocumentMetadata documentMetadata) {
    final StringBuilder sb = new StringBuilder();
    sb.append(documentMetadata.getTitle());
    sb.append(", ").append(documentMetadata.getRemainderOfTitle());
    sb.append(", ").append(documentMetadata.getIsbns());
    return sb.toString();
  }

  private boolean isValid(final DocumentMetadata documentMetadata) {
    boolean valid = documentMetadata.getIsbns() != null && documentMetadata.getIsbns().size() > 0;
    return valid;
  }

  private DocumentImportStatistics initStatistics(final LocalDate from, final LocalDate to,
      final ConnectorType connectorType) {
    DocumentImportStatistics statistics = new DocumentImportStatistics();
    statistics.setStart(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    statistics.setFromDate(from);
    statistics.setToDate(to);
    statistics.setSource(connectorType.toString());

    statistics.setErrorInInstitutionConnector(false);
    statistics.setErrorInInventoryConnector(false);
    statistics.setNrExists(0);
    statistics.setNrIgnored(0);
    statistics.setNrImported(0);
    statistics.setNrInvalid(0);
    statistics.setNrRetrieved(0);
    return statistics;
  }

  private void finishStatistics(final DocumentImportStatistics statistics) {
    statistics.setEnd(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    importStatisticsRepository.save(statistics);
    LOG.info("Import documents from {} done, range from {} to {}."
        + " Connector: {}, InventoryConnector: {}."
        + " (retrieved: {}, imported: {}, alreadyExists: {}, invalid: {}, ignored: {})",
        statistics.getSource(), statistics.getFromDate(), statistics.getToDate(),
        statistics.isErrorInInstitutionConnector() ? "ERROR" : "OK",
        statistics.isErrorInInventoryConnector() ? "ERROR" : "OK",
        statistics.getNrRetrieved(), statistics.getNrImported(), statistics
            .getNrExists(), statistics.getNrInvalid(), statistics.getNrIgnored());
  }

  /**
   * getter: inventoryConnector.
   *
   * @return the inventoryConnector
   */
  public InventoryConnector getInventoryConnector() {
    return inventoryConnector;
  }

  /**
   * setter: inventoryConnector.
   *
   * @param inventoryConnector the inventoryConnector to set
   */
  public void setInventoryConnector(final InventoryConnector inventoryConnector) {
    this.inventoryConnector = inventoryConnector;
  }

}
