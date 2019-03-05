package eu.tib.profileservice.service;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DocumentService {

  public Page<Document> findAllByExample(final Document example, final Pageable pageable);

  public Document findById(final Long id);

  public Document assignToUser(final Document document, final User user);

  public Document acceptDocument(final Long id);

  public Document rejectDocument(final Long id);

}
