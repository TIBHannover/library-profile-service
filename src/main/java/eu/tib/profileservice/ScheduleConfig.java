package eu.tib.profileservice;

import eu.tib.profileservice.connector.InstitutionConnectorFactory.ConnectorType;
import eu.tib.profileservice.scheduling.DocumentCleanupJob;
import eu.tib.profileservice.scheduling.DocumentImportJob;
import javax.annotation.PostConstruct;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "file:${envConfigDir:envConf/default/}profileservice.properties")
public class ScheduleConfig {

  private static final Logger LOG = LoggerFactory.getLogger(ScheduleConfig.class);

  @Autowired
  private Scheduler scheduler;

  @Value("${externalsystem.dnb.schedule.cron}")
  private String dnbSchedule;

  @Value("${externalsystem.bl.schedule.cron}")
  private String blSchedule;

  /**
   * Initializing document-import-jobs.
   *
   * @throws SchedulerException if the Job cannot be added to the Scheduler
   */
  @PostConstruct
  public void initDocumentImportJobs() throws SchedulerException {
    LOG.debug("init document import jobs");
    scheduleDocumentImportJob(ConnectorType.BL.toString(), blSchedule);
    scheduleDocumentImportJob(ConnectorType.DNB.toString(), dnbSchedule);
  }

  private void scheduleDocumentImportJob(final String connectorType, final String cronExpression)
      throws SchedulerException {
    final String identityPrefix = connectorType;
    if (cronExpression == null || cronExpression.length() == 0) {
      LOG.info("{} schedule not configured", identityPrefix);
      return;
    }
    LOG.info("init {} job with schedule {}", identityPrefix, cronExpression);
    JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.put(DocumentImportJob.JOB_DATA_CONNECTOR_TYPE, connectorType);
    JobDetail jobDetail = JobBuilder.newJob().ofType(DocumentImportJob.class)
        .storeDurably()
        .withIdentity(identityPrefix + "_Standard_Document_Import_Job_Detail",
            "document-import-jobs")
        .usingJobData(jobDataMap)
        .build();
    Trigger trigger = TriggerBuilder.newTrigger().forJob(jobDetail)
        .withIdentity(identityPrefix + "_Standard_Document_Import_Trigger",
            "document-import-triggers")
        .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
        .build();
    scheduler.scheduleJob(jobDetail, trigger);
  }

  /**
   * Standard Job Detail for the {@link DocumentCleanupJob}.
   *
   * @return job detail
   */
  @Bean(name = "jobDetailDocumentCleanup")
  public JobDetail jobDetailDocumentCleanup() {
    return JobBuilder.newJob().ofType(DocumentCleanupJob.class)
        .storeDurably()
        .withIdentity("Standard_Document_Cleanup_Job_Detail", "document-cleanup-jobs")
        .build();
  }

  /**
   * Standard Trigger for the {@link DocumentCleanupJob}.
   *
   * @param job job detail
   * @return trigger
   */
  @Bean
  public Trigger triggerDocumentCleanup(
      @Qualifier("jobDetailDocumentCleanup") final JobDetail job) {
    return TriggerBuilder.newTrigger().forJob(job)
        .withIdentity("Standard_Document_Cleanup_Trigger", "document-cleanup-triggers")
        .withSchedule(CronScheduleBuilder.cronSchedule("0 0 1 * * ?"))
        .build();
  }

}
