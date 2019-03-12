package eu.tib.profileservice.service;

import eu.tib.profileservice.domain.ImportFilter;
import eu.tib.profileservice.repository.ImportFilterRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link ImportFilterService}.
 */
@Service
public class ImportFilterServiceImpl implements ImportFilterService {

  @Autowired
  private ImportFilterRepository importFilterRepository;

  @Transactional
  @Override
  public ImportFilter createOrUpdate(final ImportFilter filter) {
    return importFilterRepository.save(filter);
  }

  @Transactional
  @Override
  public void delete(final ImportFilter filter) {
    importFilterRepository.delete(filter);
  }

  @Transactional(readOnly = true)
  @Override
  public List<ImportFilter> findAll() {
    return importFilterRepository.findAll();
  }

}
