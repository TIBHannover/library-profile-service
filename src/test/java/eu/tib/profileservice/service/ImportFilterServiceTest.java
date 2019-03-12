package eu.tib.profileservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.tib.profileservice.domain.ImportFilter;
import eu.tib.profileservice.domain.ImportFilter.Action;
import eu.tib.profileservice.domain.ImportFilter.ConditionType;
import eu.tib.profileservice.repository.ImportFilterRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ImportFilterServiceTest {

  @TestConfiguration
  static class TestContextConfiguration {
    @Bean
    public ImportFilterService service() {
      return new ImportFilterServiceImpl();
    }
  }

  @Autowired
  private ImportFilterService service;
  @MockBean
  private ImportFilterRepository repository;

  private ImportFilter newFilter(final Action action, final String condition,
      final ConditionType conditionType) {
    ImportFilter filter = new ImportFilter();
    filter.setAction(action);
    filter.setCondition(condition);
    filter.setConditionType(conditionType);
    return filter;
  }

  @Test
  public void testCreateOrUpdate() {
    ImportFilter filter = newFilter(Action.IGNORE, "test", ConditionType.FORM_KEYWORD);
    service.createOrUpdate(filter);
    verify(repository, times(1)).save(filter);
  }

  @Test
  public void testDelete() {
    ImportFilter filter = newFilter(Action.IGNORE, "test", ConditionType.FORM_KEYWORD);
    service.delete(filter);
    verify(repository, times(1)).delete(filter);
  }

  @Test
  public void testFindAll() {
    ImportFilter filter = newFilter(Action.IGNORE, "test", ConditionType.FORM_KEYWORD);
    when(repository.findAll()).thenReturn(Arrays.asList(new ImportFilter[] {filter}));
    List<ImportFilter> all = service.findAll();
    assertThat(all).isNotNull();
    assertThat(all.size()).isEqualTo(1);
  }

}
