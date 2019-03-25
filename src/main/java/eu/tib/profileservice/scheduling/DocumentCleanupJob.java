package eu.tib.profileservice.scheduling;

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

@Component
@DisallowConcurrentExecution
public class DocumentCleanupJob implements Job {

  private static final Logger LOG = LoggerFactory.getLogger(DocumentCleanupJob.class);

  /** Cleanup documents with creation date older than this amount of days. */
  private static final int CLEANUP_DAYS = 60;

  @Autowired
  private DocumentService documentService;

  @Override
  public void execute(final JobExecutionContext context) throws JobExecutionException {
    LOG.info("Start DocumentCleanupJob");

    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDateTime cleanupDate = utc.minusDays(CLEANUP_DAYS).toLocalDateTime();
    LOG.info("Cleanup documents created before {}", cleanupDate);
    documentService.deleteDocumentCreatedBefore(cleanupDate);

    LOG.info("DocumentCleanupJob finished");
  }
}
