package eu.tib.profileservice.service;

import eu.tib.profileservice.domain.ImportFilter;
import java.util.List;

/**
 * Manage {@link ImportFilter}s.
 */
public interface ImportFilterService {

  /**
   * Create or update the given {@link ImportFilter}.
   * 
   * @param filter filter
   * @return updated filter
   */
  public ImportFilter createOrUpdate(final ImportFilter filter);

  /**
   * Delete the given {@link ImportFilter}.
   * 
   * @param filter filter
   */
  public void delete(final ImportFilter filter);

  /**
   * Retrieve all {@link ImportFilter}s.
   * 
   * @return all filters
   */
  public List<ImportFilter> findAll();

  /**
   * Retrieve {@link ImportFilter} for the given id.
   * 
   * @param id id
   * @return filter
   */
  public ImportFilter findById(final Long id);

}
