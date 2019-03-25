package eu.tib.profileservice.service;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.User;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DocumentService {

  Page<Document> findAllByExample(Document example, Pageable pageable);

  Document findById(Long id);

  Document assignToUser(Document document, User user);

  Document acceptDocument(Long id);

  Document rejectDocument(Long id);

  void deleteDocumentCreatedBefore(LocalDateTime expiryDateUtc);

}
