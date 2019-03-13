package eu.tib.profileservice.scheduling;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.tib.profileservice.connector.InstitutionConnectorFactory.ConnectorType;
import eu.tib.profileservice.service.DocumentImportService;
import java.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class DocumentImportJobTest {

  @TestConfiguration
  static class TestContextConfiguration {

    @Bean
    public DocumentImportJob cleanupJob() {
      return new DocumentImportJob();
    }
  }

  @Autowired
  private DocumentImportJob importJob;

  @MockBean
  private DocumentImportService documentImportService;

  @MockBean
  private JobExecutionContext testContext;

  @Test
  public void testWithoutGivenDateRange() throws SchedulerException {
    when(testContext.getMergedJobDataMap()).thenReturn(new JobDataMap());
    importJob.execute(testContext);
    verify(documentImportService, times(1)).importDocuments(Mockito.any(LocalDate.class), Mockito
        .any(LocalDate.class), Mockito.any(ConnectorType.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidConnectorType() throws SchedulerException {
    JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.put(DocumentImportJob.JOB_DATA_CONNECTOR_TYPE, "invalid");
    when(testContext.getMergedJobDataMap()).thenReturn(jobDataMap);
    importJob.execute(testContext);
  }

  @Test
  public void testWithGivenDateRange() throws SchedulerException {
    JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.put(DocumentImportJob.JOB_DATA_FROM_DATE, "2019-03-05");
    jobDataMap.put(DocumentImportJob.JOB_DATA_TO_DATE, "2019-03-06");
    jobDataMap.put(DocumentImportJob.JOB_DATA_CONNECTOR_TYPE, "DNB");

    when(testContext.getMergedJobDataMap()).thenReturn(jobDataMap);
    importJob.execute(testContext);
    ArgumentCaptor<LocalDate> arg1 = ArgumentCaptor.forClass(LocalDate.class);
    ArgumentCaptor<LocalDate> arg2 = ArgumentCaptor.forClass(LocalDate.class);
    ArgumentCaptor<ConnectorType> arg3 = ArgumentCaptor.forClass(ConnectorType.class);
    verify(documentImportService, times(1)).importDocuments(arg1.capture(), arg2.capture(), arg3
        .capture());
    LocalDate expectedFromDate = LocalDate.parse("2019-03-05");
    LocalDate expectedToDate = LocalDate.parse("2019-03-06");
    assertEquals(expectedFromDate, arg1.getValue());
    assertEquals(expectedToDate, arg2.getValue());
    assertEquals(ConnectorType.DNB, arg3.getValue());
  }

}
