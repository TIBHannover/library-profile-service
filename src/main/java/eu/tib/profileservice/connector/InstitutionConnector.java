package eu.tib.profileservice.connector;

import eu.tib.profileservice.domain.DocumentMetadata;
import java.util.List;

/**
 * Connector to an institution that provides new publications.
 */
public interface InstitutionConnector {

  /**
   * Check, if there may be any further documents to retrieve. In this case
   * {@link InstitutionConnector#retrieveNextDocuments()} delivers the next documents.
   * 
   * @return true, if there may be more documents; false, otherwise
   */
  public boolean hasNext();

  /**
   * Retrieve the next list of publications.
   * 
   * @return publications
   */
  public List<DocumentMetadata> retrieveNextDocuments();

  /**
   * Check, if there was an error while retrieving documents.
   * 
   * @return true, if there was an error; false, otherwise
   */
  public boolean hasErrors();

}
