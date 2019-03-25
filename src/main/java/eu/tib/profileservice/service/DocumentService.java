package eu.tib.profileservice.service;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.User;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DocumentService {

  Page<Document> findAllByExample(final Document example, final Pageable pageable);

  Document findById(final Long id);

  Document assignToUser(final Document document, final User user);

  Document acceptDocument(final Long id);

  Document rejectDocument(final Long id);

  void deleteDocumentCreatedBefore(final LocalDateTime expiryDateUtc);

}
