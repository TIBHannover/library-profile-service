package eu.tib.profileservice.scheduling;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.tib.profileservice.service.DocumentService;
import eu.tib.profileservice.util.FileExportProcessor;
import java.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class DocumentCleanupJobTest {

  @TestConfiguration
  static class TestContextConfiguration {

    @Bean
    public DocumentCleanupJob cleanupJob() {
      return new DocumentCleanupJob();
    }
  }

  @Autowired
  private DocumentCleanupJob cleanupJob;

  @MockBean
  private DocumentService documentService;
  @MockBean
  private FileExportProcessor fileExportProcessor;

  @Test
  public void test() throws SchedulerException {
    cleanupJob.execute(null);
    verify(documentService, times(1)).deleteDocumentExpiryDateBefore(Mockito.any(
        LocalDateTime.class));
  }

}
