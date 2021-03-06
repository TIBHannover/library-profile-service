package eu.tib.profileservice.service;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.Document.Status;
import eu.tib.profileservice.domain.DocumentSearch;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DocumentService {

  Page<Document> findAllByExample(Document example, Pageable pageable);

  long countByExample(Document example);

  /**
   * Find all {@link Document}s by the given {@link DocumentSearch}.
   * Null-values will be ignored (like findByExample).
   * @param search the search
   * @param pageable pageable
   * @return documents
   */
  Page<Document> findAllByDocumentSearch(DocumentSearch search, Pageable pageable);

  Document findById(Long id);

  Document findByMetadataIsbnsContains(String isbn);

  /**
   * Assign document to the user identified by the given userid.
   * @param document document to assign
   * @param assigneeId id of the user the document should be assigned to
   * @return modified document
   */
  Document assignToUser(Document document, Long assigneeId);

  Document acceptDocument(Long id);

  Document rejectDocument(Long id);

  /**
   * Persist the given {@link Document}.
   * @param document document to persist
   * @return updated document
   */
  Document saveDocument(Document document);

  /**
   * Delete documents with expiry date before the given date.
   * @param expiryDateUtc expiry date
   */
  void deleteDocumentExpiryDateBefore(LocalDateTime expiryDateUtc);

  /**
   * Process an export for all {@link Document}s with status {@link Status#ACCEPTED}.
   * @return true, if the export was successful; false, otherwise
   */
  boolean export();

}
