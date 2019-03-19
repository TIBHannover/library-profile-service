package eu.tib.profileservice.scheduling;

import eu.tib.profileservice.connector.InstitutionConnectorFactory.ConnectorType;
import eu.tib.profileservice.service.DocumentImportService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
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
  /** Parameter name of job data: connector type. */
  public static final String JOB_DATA_CONNECTOR_TYPE = "connectorType";

  @Autowired
  private DocumentImportService documentImportService;

  @Override
  public void execute(final JobExecutionContext context) throws JobExecutionException {
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
      from = utc.minusDays(7).toLocalDate();
      to = yesterday;
    }
    String connectorType = jobDataMap.getString(JOB_DATA_CONNECTOR_TYPE);
    List<ConnectorType> connectors;
    if (connectorType != null) {
      connectors = Arrays.asList(ConnectorType.valueOf(connectorType));
    } else {
      connectors = Arrays.asList(ConnectorType.values());
    }
    LOG.debug("from: {}, to: {}, connectors: {}", from, to, connectors);

    connectors.stream().forEach(c -> documentImportService.importDocuments(from, to, c));

    LOG.info("DocumentImportJob finished");
  }

}
