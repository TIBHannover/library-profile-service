package eu.tib.profileservice.connector;

import eu.tib.profileservice.domain.DocumentMetadata;
import java.time.LocalDate;
import java.util.List;

public interface InstitutionConnector {

  public List<DocumentMetadata> retrieveDocuments(final LocalDate from, final LocalDate to);

}
