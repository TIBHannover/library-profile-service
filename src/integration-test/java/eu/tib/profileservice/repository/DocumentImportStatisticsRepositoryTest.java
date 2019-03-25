package eu.tib.profileservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import eu.tib.profileservice.domain.DocumentImportStatistics;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
public class DocumentImportStatisticsRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;
  @Autowired
  private DocumentImportStatisticsRepository importStatisticsRepository;

  private DocumentImportStatistics newStatistics() {
    DocumentImportStatistics statistics = new DocumentImportStatistics();
    statistics.setStart(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    statistics.setFromDate(LocalDate.parse("2019-03-25"));
    statistics.setToDate(LocalDate.parse("2019-03-25"));
    statistics.setSource("TEST");

    statistics.setErrorInInstitutionConnector(false);
    statistics.setErrorInInventoryConnector(false);
    statistics.setNrExists(0);
    statistics.setNrIgnored(0);
    statistics.setNrImported(0);
    statistics.setNrInvalid(0);
    statistics.setNrRetrieved(0);
    return statistics;
  }


  /**
   * Before.
   */
  @Before
  public void before() {
    entityManager.flush();
    entityManager.getEntityManager().createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE")
        .executeUpdate();
    entityManager.getEntityManager().createNativeQuery("TRUNCATE TABLE "
        + DocumentImportStatistics.ENTITY_NAME)
        .executeUpdate();
    entityManager.getEntityManager().createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE")
        .executeUpdate();
  }

  @Test
  public void testCreate() {
    importStatisticsRepository.save(newStatistics());

    List<DocumentImportStatistics> all = importStatisticsRepository.findAll();
    assertThat(all).isNotNull();
    assertThat(all.size()).isEqualTo(1);
  }

  @Test
  public void testUpdate() {
    DocumentImportStatistics statistics = newStatistics();
    entityManager.persist(statistics);

    List<DocumentImportStatistics> all = importStatisticsRepository.findAll();
    assertThat(all).isNotNull();
    assertThat(all.size()).isEqualTo(1);
    statistics = all.get(0);
    statistics.setSource("test2");
    statistics.setErrorInInstitutionConnector(true);
    importStatisticsRepository.save(statistics);

    all = importStatisticsRepository.findAll();
    assertThat(all).isNotNull();
    assertThat(all.size()).isEqualTo(1);
    assertThat(all.get(0).getSource()).isEqualTo("test2");
    assertThat(all.get(0).isErrorInInstitutionConnector()).isEqualTo(true);
  }

  @Test
  public void testDelete() {
    DocumentImportStatistics statistics = newStatistics();
    entityManager.persist(statistics);

    List<DocumentImportStatistics> all = importStatisticsRepository.findAll();
    assertThat(all).isNotNull();
    assertThat(all.size()).isEqualTo(1);
    statistics = all.get(0);
    importStatisticsRepository.delete(statistics);
    all = importStatisticsRepository.findAll();
    assertThat(all).isNotNull();
    assertThat(all.size()).isEqualTo(0);
  }
}
