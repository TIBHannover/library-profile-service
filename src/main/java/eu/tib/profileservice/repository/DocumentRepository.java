package eu.tib.profileservice.repository;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.User;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
  public Document findByMetadataIsbns(final String isbn);

  public Page<Document> findAllByAssignee(final User assignee, final Pageable pageable);

  public void deleteByCreationDateUtcBefore(final LocalDateTime expiryDate);
}
