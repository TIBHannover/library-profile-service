package eu.tib.profileservice.connector;

import eu.tib.profileservice.domain.DocumentMetadata;
import java.util.List;

public interface InstitutionConnector {

  public boolean hasNext();

  public List<DocumentMetadata> retrieveNextDocuments();

}
