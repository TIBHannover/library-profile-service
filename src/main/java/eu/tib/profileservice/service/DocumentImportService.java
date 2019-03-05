package eu.tib.profileservice.service;

import java.time.LocalDate;

public interface DocumentImportService {

  public void importDocuments(final LocalDate from, final LocalDate to);

}
