package eu.tib.profileservice.scheduling;

import eu.tib.profileservice.service.DocumentImportService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link Job} that calls the {@link DocumentImportService}.
 * <p>
 * Date range may be given in the job details. Standard date range is <i>yesterday - yesterday</i>.
 * </p>
 */
@Component
@DisallowConcurrentExecution
public class DocumentImportJob implements Job {

  private static final Logger LOG = LoggerFactory.getLogger(DocumentImportJob.class);

  /** Parameter name of job data: from date. */
  public static final String JOB_DATA_FROM_DATE = "fromDate";
  /** Parameter name of job data: to date. */
  public static final String JOB_DATA_TO_DATE = "toDate";

  @Autowired
  private DocumentImportService documentImportService;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    LOG.info("Start DocumentImportJob");
    LocalDate from;
    LocalDate to;
    JobDataMap jobDataMap = context.getMergedJobDataMap();
    String fromDate = jobDataMap.getString(JOB_DATA_FROM_DATE);
    String toDate = jobDataMap.getString(JOB_DATA_TO_DATE);
    if (fromDate != null && toDate != null) {
      from = LocalDate.parse(fromDate);
      to = LocalDate.parse(toDate);
    } else {
      OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
      LocalDate yesterday = utc.minusDays(1).toLocalDate();
      from = yesterday;
      to = yesterday;
    }

    documentImportService.importDocuments(from, to);

    LOG.info("DocumentImportJob finished");
  }

}