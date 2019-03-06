package eu.tib.profileservice.service;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.Document.Status;
import eu.tib.profileservice.domain.User;
import eu.tib.profileservice.repository.DocumentRepository;
import eu.tib.profileservice.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentServiceImpl implements DocumentService {

  private static final Logger LOG = LoggerFactory.getLogger(DocumentServiceImpl.class);

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private DocumentRepository documentRepository;

  @Transactional(readOnly = true)
  @Override
  public Page<Document> findAllByExample(Document example, Pageable pageable) {
    return documentRepository.findAll(Example.of(example), pageable);
  }

  @Transactional(readOnly = true)
  @Override
  public Document findById(final Long id) {
    if (id == null) {
      return null;
    }
    try {
      return documentRepository.findById(id).get();
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  @Transactional
  @Override
  public Document assignToUser(final Document document, final User user) {
    if (document == null || user == null || user.getId() == null) {
      LOG.debug("document/user cannot be null");
      return null;
    }
    final Document persistedDocument = findById(document.getId());
    final User persistedUser = userRepository.getOne(user.getId());
    if (persistedDocument == null || persistedUser == null) {
      LOG.debug("cannot find document/user");
      return null;
    }
    persistedDocument.setAssignee(persistedUser);
    return documentRepository.save(persistedDocument);
  }

  @Transactional
  @Override
  public Document acceptDocument(Long id) {
    if (id == null) {
      return null;
    }
    final Document document = documentRepository.getOne(id);
    document.setStatus(Status.ACCEPTED);
    return documentRepository.save(document);
  }

  @Transactional
  @Override
  public Document rejectDocument(Long id) {
    if (id == null) {
      return null;
    }
    final Document document = documentRepository.getOne(id);
    document.setStatus(Status.REJECTED);
    return documentRepository.save(document);
  }

  @Override
  public void deleteDocumentCreatedBefore(LocalDateTime expiryDateUtc) {
    documentRepository.deleteByCreationDateUtcBefore(expiryDateUtc);
  }

}
