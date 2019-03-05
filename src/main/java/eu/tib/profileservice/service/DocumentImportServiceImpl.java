package eu.tib.profileservice.service;

import static eu.tib.profileservice.connector.InstitutionConnectorFactory.ConnectorType.DNB;

import eu.tib.profileservice.connector.InstitutionConnector;
import eu.tib.profileservice.connector.InstitutionConnectorFactory;
import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.Document.Status;
import eu.tib.profileservice.domain.DocumentMetadata;
import eu.tib.profileservice.repository.DocumentRepository;
import eu.tib.profileservice.util.DocumentAssignmentFinder;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentImportServiceImpl implements DocumentImportService {

  private static final Logger LOG = LoggerFactory.getLogger(DocumentImportServiceImpl.class);

  private class ImportStatistics {
    private int retrieved = 0;
    private int alreadyExists = 0;
    private int invalid = 0;
    private int ignored = 0;
    private int imported = 0;
  }

  @Autowired
  private InstitutionConnectorFactory connectorFactory;

  @Autowired
  private UserService userService;

  @Autowired
  private DocumentRepository documentRepository;

  @Override
  public void importDocuments(LocalDate from, LocalDate to) {
    DocumentAssignmentFinder documentAssignmentFinder = new DocumentAssignmentFinder(userService
        .findAll());

    LOG.info("Import documents from DNB (from: {}, to: {})", from, to);
    ImportStatistics dnbStatistics = new ImportStatistics();
    InstitutionConnector dnbConn = connectorFactory.createConnector(DNB, from, to);
    while (dnbConn.hasNext()) {
      LOG.debug("going for next request (retrieved {} documents yet)", dnbStatistics.retrieved);
      List<DocumentMetadata> documents = dnbConn.retrieveNextDocuments();
      if (documents == null) {
        LOG.error("Cannot retrieve documents from DNB");
        break;
      } else {
        dnbStatistics.retrieved += documents.size();
        documents.forEach(doc -> createNewDocument(doc, documentAssignmentFinder, dnbStatistics));
      }
    }
    LOG.info(
        "Import documents from DNB done."
            + " (retrieved: {}, imported: {}, alreadyExists: {}, invalid: {}, ignored: {}",
        dnbStatistics.retrieved, dnbStatistics.imported, dnbStatistics.alreadyExists,
        dnbStatistics.invalid, dnbStatistics.ignored);
  }

  /**
   * Persist a new {@link Document} for the given {@link DocumentMetadata}, if not already existing.
   */
  private void createNewDocument(final DocumentMetadata documentMetadata,
      final DocumentAssignmentFinder documentAssignmentFinder, final ImportStatistics statistics) {
    if (!isValid(documentMetadata)) {
      LOG.error("invalid document: {}", buildDocumentMetadataString(documentMetadata));
      statistics.invalid++;
      return;
    }
    Document existingDocument = findExistingDocument(documentMetadata);
    if (existingDocument != null) {
      //LOG.debug("document already exists: {}", buildDocumentMetadataString(documentMetadata));
      statistics.alreadyExists++;
    } else {
      Document document = new Document();
      OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
      document.setCreationDateUtc(utc.toLocalDateTime());
      document.setMetadata(documentMetadata);
      if (shouldIgnore(documentMetadata)) {
        document.setStatus(Status.IGNORED);
        statistics.ignored++;
      } else {
        document.setStatus(Status.IN_PROGRESS);
        document.setAssignee(documentAssignmentFinder.determineAssignee(documentMetadata));
        statistics.imported++;
      }
      document = documentRepository.save(document);
      //LOG.debug("document imported: {}", buildDocumentMetadataString(documentMetadata));
    }
  }


  /**
   * Check if the given document should be ignored.
   * 
   * @param documentMetadata document to check
   * @return true, if the document should be ignored; false otherwise
   */
  private boolean shouldIgnore(final DocumentMetadata documentMetadata) {
    // TODO rules (ignore new documents)
    return false;
  }

  /**
   * Find the already existing {@link Document} that matches the given {@link DocumentMetadata}.
   * 
   * @param documentMetadata has to match this document
   * @return the {@link Document}, if existing; null otherwise
   */
  private Document findExistingDocument(final DocumentMetadata documentMetadata) {
    for (String isbn : documentMetadata.getIsbns()) {
      Document existingDocument = documentRepository.findByMetadataIsbns(isbn);
      if (existingDocument != null) {
        return existingDocument;
      }
    }
    return null;
  }

  private String buildDocumentMetadataString(final DocumentMetadata documentMetadata) {
    final StringBuilder sb = new StringBuilder();
    sb.append(documentMetadata.getTitle());
    sb.append(", ").append(documentMetadata.getRemainderOfTitle());
    sb.append(", ").append(documentMetadata.getAuthor());
    sb.append(", ").append(documentMetadata.getIsbns());
    return sb.toString();
  }

  private boolean isValid(final DocumentMetadata documentMetadata) {
    // TODO
    boolean valid = documentMetadata.getIsbns() != null && documentMetadata.getIsbns().size() > 0;

    return valid;
  }

}
