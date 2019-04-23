package eu.tib.profileservice.repository;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.Document.Status;
import eu.tib.profileservice.domain.DocumentSearch;
import eu.tib.profileservice.domain.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<Document, Long>,
    JpaSpecificationExecutor<Document> {
  // info: have to use a custom query here, because the generated sql is incorrect
  // (uses wildcards for the string parameter ('%string%' in (select isbns...)))
  @Query("select d from " + Document.ENTITY_NAME + " d WHERE :isbn in elements(d.metadata.isbns)")
  Document findByMetadataIsbnsContains(@Param("isbn") String isbn);

  /**
   * Set new status of all {@link Document} with the given status.
   * @param oldStatus status of the documents to select
   * @param newStatus status to set
   * @return number of updated documents
   */
  @Modifying(clearAutomatically = true)
  @Query("UPDATE " + Document.ENTITY_NAME
      + " d SET d.status = :newStatus WHERE d.status = :oldStatus")
  int updateStatus(@Param("oldStatus") Status oldStatus, @Param("newStatus") Status newStatus);

  /**
   * Find all {@link Document}s by status.
   * @param status status
   * @return document
   */
  List<Document> findAllByStatus(Status status);

  Page<Document> findAllByAssignee(User assignee, Pageable pageable);

  /**
   * Delete documents with expiry date before the given date.
   * @param expiryDate expiry date
   */
  void deleteByExpiryDateUtcBefore(LocalDateTime expiryDate);

  /**
   * Find all {@link Document}s by the given {@link DocumentSearch}.
   * Null-values will be ignored (like findByExample).
   * @param documentSearch the search
   * @param pageable pageable
   * @return documents
   */
  default Page<Document> findAllByDocumentSearch(final DocumentSearch documentSearch,
      final Pageable pageable) {
    Specification<Document> spec;
    spec = DocumentSpecification.byAssignee(documentSearch.getAssignee());
    spec = spec.and(DocumentSpecification.byStatus(documentSearch.getStatus()));
    spec = spec.and(DocumentSpecification.byCreationDateAfterOrEqual(documentSearch
        .getCreationDateFrom()));
    spec = spec.and(DocumentSpecification.byCreationDateBeforeOrEqual(documentSearch
        .getCreationDateTo()));
    return findAll(spec, pageable);
  }
}
