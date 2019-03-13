package eu.tib.profileservice.service;

import eu.tib.profileservice.connector.InstitutionConnectorFactory.ConnectorType;
import java.time.LocalDate;

public interface DocumentImportService {

  public void importDocuments(final LocalDate from, final LocalDate to,
      final ConnectorType connectorType);

}
