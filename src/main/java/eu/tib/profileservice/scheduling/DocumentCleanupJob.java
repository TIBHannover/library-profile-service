package eu.tib.profileservice.scheduling;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.service.DocumentService;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Job that cleans up expired {@link Document}s.
 */
@Component
@DisallowConcurrentExecution
public class DocumentCleanupJob implements Job {

  private static final Logger LOG = LoggerFactory.getLogger(DocumentCleanupJob.class);

  @Autowired
  private DocumentService documentService;

  @Override
  public void execute(final JobExecutionContext context) throws JobExecutionException {
    LOG.info("Start DocumentCleanupJob");

    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDateTime now = utc.toLocalDateTime();
    LOG.info("Cleanup documents with expiry date before {}", now);
    documentService.deleteDocumentExpiryDateBefore(now);

    LOG.info("DocumentCleanupJob finished");
  }
}
