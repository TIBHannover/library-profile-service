package eu.tib.profileservice.repository;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.User;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<Document, Long> {
  // info: have to use a custom query here, because the generated sql is incorrect
  // (uses wildcards for the string parameter ('%string%' in (select isbns...)))
  @Query("select d from " + Document.ENTITY_NAME + " d WHERE :isbn in elements(d.metadata.isbns)")
  Document findByMetadataIsbnsContains(@Param("isbn") String isbn);

  Page<Document> findAllByAssignee(User assignee, Pageable pageable);

  void deleteByCreationDateUtcBefore(LocalDateTime expiryDate);
}
