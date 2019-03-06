package eu.tib.profileservice;

import eu.tib.profileservice.scheduling.DocumentCleanupJob;
import eu.tib.profileservice.scheduling.DocumentImportJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScheduleConfig {

  /**
   * Standard Job Detail for the {@link DocumentImportJob}.
   * 
   * @return job detail
   */
  @Bean(name = "jobDetailDocumentImport")
  public JobDetail jobDetailDocumentImport() {
    return JobBuilder.newJob().ofType(DocumentImportJob.class)
        .storeDurably()
        .withIdentity("Standard_Document_Import_Job_Detail", "document-import-jobs")
        .build();
  }

  /**
   * Standard Trigger for the {@link DocumentImportJob}.
   * 
   * @param job job detail
   * @return trigger
   */
  @Bean
  public Trigger triggerDocumentImport(@Qualifier("jobDetailDocumentImport") JobDetail job) {
    return TriggerBuilder.newTrigger().forJob(job)
        .withIdentity("Standard_Document_Import_Trigger", "document-import-triggers")
        .withSchedule(CronScheduleBuilder.cronSchedule("0 0 3 * * ?"))
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
  public Trigger triggerDocumentCleanup(@Qualifier("jobDetailDocumentCleanup") JobDetail job) {
    return TriggerBuilder.newTrigger().forJob(job)
        .withIdentity("Standard_Document_Cleanup_Trigger", "document-cleanup-triggers")
        .withSchedule(CronScheduleBuilder.cronSchedule("0 0 1 * * ?"))
        .build();
  }

}
