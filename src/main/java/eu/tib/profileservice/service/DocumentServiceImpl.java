package eu.tib.profileservice.service;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.Document.Status;
import eu.tib.profileservice.domain.DocumentSearch;
import eu.tib.profileservice.domain.User;
import eu.tib.profileservice.repository.DocumentRepository;
import eu.tib.profileservice.repository.UserRepository;
import eu.tib.profileservice.util.FileExportProcessor;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentServiceImpl implements DocumentService {

  private static final Logger LOG = LoggerFactory.getLogger(DocumentServiceImpl.class);

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private DocumentRepository documentRepository;
  @Autowired
  private FileExportProcessor fileExportProcessor;

  @Transactional(readOnly = true)
  @Override
  public Page<Document> findAllByExample(final Document example, final Pageable pageable) {
    return documentRepository.findAll(Example.of(example), pageable);
  }

  @Transactional(readOnly = true)
  @Override
  public Page<Document> findAllByDocumentSearch(final DocumentSearch search,
      final Pageable pageable) {
    return documentRepository.findAllByDocumentSearch(search, pageable);
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
  public Document acceptDocument(final Long id) {
    return updateDocumentStatus(id, Status.ACCEPTED);
  }

  @Transactional
  @Override
  public Document rejectDocument(final Long id) {
    return updateDocumentStatus(id, Status.REJECTED);
  }

  private Document updateDocumentStatus(final Long id, final Status status) {
    if (id == null) {
      return null;
    }
    final Document document = documentRepository.getOne(id);
    document.setStatus(status);
    return documentRepository.save(document);
  }

  @Transactional
  @Override
  public void deleteDocumentExpiryDateBefore(final LocalDateTime expiryDateUtc) {
    documentRepository.deleteByExpiryDateUtcBefore(expiryDateUtc);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public Document saveDocument(final Document document) {
    return documentRepository.save(document);
  }

  @Transactional
  @Override
  public boolean export() {
    int count = documentRepository.updateStatus(Status.ACCEPTED, Status.EXPORTING);
    if (count == 0) {
      LOG.debug("no document ready for export");
      return false;
    }
    LOG.debug("exporting {} documents", count);
    List<Document> documents = documentRepository.findAllByStatus(Status.EXPORTING);
    try {
      LOG.debug("write {} documents to file", documents.size());
      fileExportProcessor.export(documents);
    } catch (IOException e) {
      LOG.error("error while exporting documents", e);
      documentRepository.updateStatus(Status.EXPORTING, Status.ACCEPTED);
      return false;
    }
    count = documentRepository.updateStatus(Status.EXPORTING, Status.EXPORTED);
    LOG.debug("exported {} documents", count);
    return true;
  }

  @Transactional(readOnly = true)
  @Override
  public long countByExample(final Document example) {
    return documentRepository.count(Example.of(example));
  }

}
