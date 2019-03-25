package eu.tib.profileservice.service;

import eu.tib.profileservice.connector.InstitutionConnectorFactory.ConnectorType;
import eu.tib.profileservice.connector.InventoryConnector;
import eu.tib.profileservice.domain.Document.Status;
import eu.tib.profileservice.domain.ImportFilter;
import java.time.LocalDate;

/**
 * Services that manages the document import.
 */
public interface DocumentImportService {

  /**
   * Import documents from the given external system for the given date range. Import document if
   * and only if the document is not contained in the own inventory already.
   * <p>
   * If {@link InventoryConnector} configured, check for already existing documents in the (remote)
   * inventory.
   * </p>
   * <p>
   * If the document matches any {@link ImportFilter}, then the action of the filter will be
   * processed (set status to {@link Status#IGNORED},...).
   * </p>
   * 
   * @param from from date
   * @param to to date
   * @param connectorType type of the external system the docs should be imported from
   */
  void importDocuments(final LocalDate from, final LocalDate to, final ConnectorType connectorType);

}
