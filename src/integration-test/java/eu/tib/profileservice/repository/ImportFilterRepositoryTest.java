package eu.tib.profileservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import eu.tib.profileservice.domain.ImportFilter;
import eu.tib.profileservice.domain.ImportFilter.Action;
import eu.tib.profileservice.domain.ImportFilter.ConditionType;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ImportFilterRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;
  @Autowired
  private ImportFilterRepository importFilterRepository;

  private ImportFilter newFilter(final Action action, final String condition,
      final ConditionType conditionType) {
    ImportFilter filter = new ImportFilter();
    filter.setAction(action);
    filter.setCondition(condition);
    filter.setConditionType(conditionType);
    return filter;
  }

  /**
   * Before.
   */
  @Before
  public void before() {
    entityManager.flush();
    entityManager.getEntityManager().createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE")
        .executeUpdate();
    entityManager.getEntityManager().createNativeQuery("TRUNCATE TABLE " + ImportFilter.ENTITY_NAME)
        .executeUpdate();
    entityManager.getEntityManager().createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE")
        .executeUpdate();
  }

  @Test
  public void testCreate() {
    final ImportFilter filter = newFilter(Action.IGNORE, "test", ConditionType.FORM_KEYWORD);
    importFilterRepository.save(filter);

    List<ImportFilter> all = importFilterRepository.findAll();
    assertThat(all).isNotNull();
    assertThat(all.size()).isEqualTo(1);
  }

  @Test
  public void testUpdate() {
    ImportFilter filter = newFilter(Action.IGNORE, "test1", ConditionType.FORM_KEYWORD);
    entityManager.persist(filter);

    List<ImportFilter> all = importFilterRepository.findAll();
    assertThat(all).isNotNull();
    assertThat(all.size()).isEqualTo(1);
    filter = all.get(0);
    filter.setCondition("test2");
    filter.setConditionType(ConditionType.CATEGORY);
    importFilterRepository.save(filter);

    all = importFilterRepository.findAll();
    assertThat(all).isNotNull();
    assertThat(all.size()).isEqualTo(1);
    assertThat(all.get(0).getCondition()).isEqualTo("test2");
    assertThat(all.get(0).getConditionType()).isEqualTo(ConditionType.CATEGORY);
  }

  @Test
  public void testDelete() {
    ImportFilter filter = newFilter(Action.IGNORE, "test", ConditionType.FORM_KEYWORD);
    entityManager.persist(filter);

    List<ImportFilter> all = importFilterRepository.findAll();
    assertThat(all).isNotNull();
    assertThat(all.size()).isEqualTo(1);
    filter = all.get(0);
    importFilterRepository.delete(filter);
    all = importFilterRepository.findAll();
    assertThat(all).isNotNull();
    assertThat(all.size()).isEqualTo(0);
  }
}
