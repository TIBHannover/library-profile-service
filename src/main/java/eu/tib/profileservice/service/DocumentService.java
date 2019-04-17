package eu.tib.profileservice.service;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.DocumentSearch;
import eu.tib.profileservice.domain.User;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DocumentService {

  Page<Document> findAllByExample(Document example, Pageable pageable);

  /**
   * Find all {@link Document}s by the given {@link DocumentSearch}.
   * Null-values will be ignored (like findByExample).
   * @param search the search
   * @param pageable pageable
   * @return documents
   */
  Page<Document> findAllByDocumentSearch(DocumentSearch search, Pageable pageable);

  Document findById(Long id);

  Document assignToUser(Document document, User user);

  Document acceptDocument(Long id);

  Document rejectDocument(Long id);

  /**
   * Delete documents with expiry date before the given date.
   * @param expiryDateUtc expiry date
   */
  void deleteDocumentExpiryDateBefore(LocalDateTime expiryDateUtc);

}
