package eu.tib.profileservice.connector;

import eu.tib.profileservice.domain.DocumentMetadata;

/**
 * Connector to own inventory. Used to check whether documents already exist.
 */
public interface InventoryConnector {

  /**
   * Check if the given {@link DocumentMetadata} does already exist in the inventory.
   * 
   * @param documentMetadata the {@link DocumentMetadata}
   * @return true, if the inventory contains the document; false, otherwise
   * @throws ConnectorException thrown when there was an error while retrieving the data
   */
  boolean contains(final DocumentMetadata documentMetadata) throws ConnectorException;

}
