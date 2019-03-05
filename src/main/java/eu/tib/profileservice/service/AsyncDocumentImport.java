package eu.tib.profileservice.service;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Just a helper class to test the document import via browser asynchronously.
 */
@Service
public class AsyncDocumentImport {

  @Autowired
  private DocumentImportService documentImportService;

  @Async
  public void importDocuments(LocalDate from, LocalDate to) {
    documentImportService.importDocuments(from, to);
  }

}
