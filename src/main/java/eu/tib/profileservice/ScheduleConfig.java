package eu.tib.profileservice;

import eu.tib.profileservice.connector.InstitutionConnectorFactory.ConnectorType;
import eu.tib.profileservice.scheduling.DocumentCleanupJob;
import eu.tib.profileservice.scheduling.DocumentImportJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "file:${envConfigDir:envConf/default/}profileservice.properties")
public class ScheduleConfig {

  private static final Logger LOG = LoggerFactory.getLogger(ScheduleConfig.class);

  @Bean(name = "jobDetailDnbImport")
  public JobDetail jobDetailDnbImport() {
    return documentImportJobDetail(ConnectorType.DNB.toString());
  }

  @Bean
  public Trigger triggerDnbImport(@Qualifier("jobDetailDnbImport") final JobDetail job,
      @Value("${externalsystem.dnb.schedule.cron}") final String schedule) {
    return documentImportTrigger(job, ConnectorType.DNB.toString(), schedule);
  }

  @Bean(name = "jobDetailBlImport")
  public JobDetail jobDetailBlImport() {
    return documentImportJobDetail(ConnectorType.BL.toString());
  }

  @Bean
  public Trigger triggerBlImport(@Qualifier("jobDetailBlImport") final JobDetail job,
      @Value("${externalsystem.bl.schedule.cron}") final String schedule) {
    return documentImportTrigger(job, ConnectorType.BL.toString(), schedule);
  }

  private JobDetail documentImportJobDetail(final String connectorType) {
    JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.put(DocumentImportJob.JOB_DATA_CONNECTOR_TYPE, connectorType);
    return JobBuilder.newJob().ofType(DocumentImportJob.class)
        .storeDurably()
        .withIdentity(connectorType + "_Standard_Document_Import_Job_Detail",
            "document-import-jobs")
        .usingJobData(jobDataMap)
        .build();
  }

  private Trigger documentImportTrigger(final JobDetail jobDetail, final String connectorType,
      final String cronExpression) {
    if (cronExpression == null || cronExpression.length() == 0) {
      LOG.info("{} schedule not configured", connectorType);
      return null;
    }
    LOG.info("init {} job with schedule {}", connectorType, cronExpression);
    return TriggerBuilder.newTrigger().forJob(jobDetail)
        .withIdentity(connectorType + "_Standard_Document_Import_Trigger",
            "document-import-triggers")
        .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
        .build();
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
