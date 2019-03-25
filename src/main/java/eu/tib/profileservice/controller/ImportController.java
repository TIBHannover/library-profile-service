package eu.tib.profileservice.controller;

import static eu.tib.profileservice.controller.HomeController.ATTRIBUTE_INFO_MESSAGE;
import static eu.tib.profileservice.controller.HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE;
import static eu.tib.profileservice.controller.HomeController.INFO_MESSAGE_TYPE_ERROR;
import static eu.tib.profileservice.controller.HomeController.INFO_MESSAGE_TYPE_SUCCESS;

import eu.tib.profileservice.connector.InstitutionConnectorFactory.ConnectorType;
import eu.tib.profileservice.domain.ImportFilter;
import eu.tib.profileservice.scheduling.DocumentImportJob;
import eu.tib.profileservice.service.ImportFilterService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for import configuration, for example {@link ImportFilter}.
 */
@Controller
@RequestMapping(value = ImportController.BASE_PATH)
public class ImportController {

  private static final Logger LOG = LoggerFactory.getLogger(ImportController.class);

  protected static final String CODE_MESSAGE_FILTER_NOTFOUND = "message.filter.notfound";
  protected static final String CODE_MESSAGE_FILTER_CREATED = "message.filter.save.success";
  protected static final String CODE_MESSAGE_FILTER_DELETED = "message.filter.delete.success";
  protected static final String CODE_MESSAGE_SCHEDULE_DOCUMENTIMPORT_FAILED =
      "message.schedule.documentimport.failed";

  public static final String BASE_PATH = "/import";
  public static final String PATH_LISTFILTER = "/listfilters";
  public static final String PATH_SHOWFILTER = "/showfilter";
  public static final String PATH_CREATEFILTER = "/createfilter";
  public static final String PATH_EDITFILTER = "/editfilter";
  public static final String PATH_SAVE_FILTER = "/savefilter";
  public static final String PATH_DELETE_FILTER = "/deletefilter";
  public static final String PATH_IMPORT = "/import";

  public static final String BASE_URL_TEMPLATE = "import";
  public static final String TEMPLATE_LISTFILTER = "/listfilters";
  public static final String TEMPLATE_SHOWFILTER = "/showfilter";
  public static final String TEMPLATE_IMPORT = "/import";
  public static final String TEMPLATE_CREATE_OR_EDIT_FILTER = "/createOrEditFilter";

  @Autowired
  private ImportFilterService importFilterService;
  @Autowired
  private Scheduler scheduler;

  @ModelAttribute("actionSaveFilter")
  public String populateActionAdd() {
    return BASE_PATH + PATH_SAVE_FILTER;
  }

  @ModelAttribute("availableFilterConditionTypes")
  public ImportFilter.ConditionType[] populateAvailableFilterConditionTypes() {
    return ImportFilter.ConditionType.values();
  }

  @ModelAttribute("availableFilterActions")
  public ImportFilter.Action[] populateAvailableFilterActions() {
    return ImportFilter.Action.values();
  }

  @GetMapping("**")
  public String index() {
    return "redirect:" + BASE_PATH + PATH_LISTFILTER;
  }

  /**
   * List.
   *
   * @param model model
   * @return template
   */
  @GetMapping(PATH_LISTFILTER)
  public String list(final Model model) {
    List<ImportFilter> filters = importFilterService.findAll();
    model.addAttribute("filters", filters);
    return BASE_URL_TEMPLATE + TEMPLATE_LISTFILTER;
  }

  /**
   * Show filter.
   *
   * @param filterId id of the filter to show
   * @param model model
   * @param redirectAttrs redirectAttrs
   * @return template
   */
  @GetMapping(PATH_SHOWFILTER + "/{filterId}")
  public String showFilter(@PathVariable("filterId") final Long filterId, final Model model,
      final RedirectAttributes redirectAttrs) {
    ImportFilter filter = importFilterService.findById(filterId);
    if (filter == null) {
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_FILTER_NOTFOUND);
      return "redirect:" + BASE_PATH + PATH_LISTFILTER;
    }
    model.addAttribute("filter", filter);
    return BASE_URL_TEMPLATE + TEMPLATE_SHOWFILTER;
  }

  /**
   * Create new filter template.
   *
   * @param filter filter
   * @param model model
   * @return template
   */
  @GetMapping(PATH_CREATEFILTER)
  public String addFilter(final ImportFilter filter, final Model model) {
    model.addAttribute("filter", filter);
    return BASE_URL_TEMPLATE + TEMPLATE_CREATE_OR_EDIT_FILTER;
  }

  /**
   * Edit existing filter template.
   *
   * @param filterId id of the {@link ImportFilter}
   * @param model model
   * @param redirectAttrs redirectAttrs
   * @return template
   */
  @GetMapping(PATH_EDITFILTER + "/{filterId}")
  public String editFilter(@PathVariable("filterId") final Long filterId, final Model model,
      final RedirectAttributes redirectAttrs) {
    ImportFilter filter = importFilterService.findById(filterId);
    if (filter == null) {
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_FILTER_NOTFOUND);
      return "redirect:" + BASE_PATH + PATH_LISTFILTER;
    }
    model.addAttribute("edit", true);
    model.addAttribute("filter", filter);
    return BASE_URL_TEMPLATE + TEMPLATE_CREATE_OR_EDIT_FILTER;
  }

  /**
   * Save method for {@link ImportFilter} (create or edit).
   *
   * @param filter filter
   * @param model model
   * @param redirectAttrs redirectAttrs
   * @param bindingResult bindingResult
   * @return template
   */
  @RequestMapping(value = PATH_SAVE_FILTER, method = RequestMethod.POST)
  public String saveFilter(final ImportFilter filter, final Model model,
      final RedirectAttributes redirectAttrs,
      final BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return BASE_URL_TEMPLATE + TEMPLATE_CREATE_OR_EDIT_FILTER;
    }
    importFilterService.createOrUpdate(filter);
    redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_SUCCESS);
    redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_FILTER_CREATED);
    return "redirect:" + BASE_PATH + PATH_LISTFILTER;
  }

  /**
   * Delete method for {@link ImportFilter}.
   *
   * @param filter filter
   * @param model model
   * @param redirectAttrs redirectAttrs
   * @return
   */
  @RequestMapping(value = PATH_DELETE_FILTER, method = RequestMethod.POST)
  public String deleteFilter(final ImportFilter filter, final Model model,
      final RedirectAttributes redirectAttrs) {
    importFilterService.delete(filter);
    redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_SUCCESS);
    redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_FILTER_DELETED);
    return "redirect:" + BASE_PATH + PATH_LISTFILTER;
  }

  /**
   * Just to test the import via browser.
   *
   * @param model model
   * @return template
   */
  @GetMapping(PATH_IMPORT)
  public String importDocuments(final Model model) {
    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    model.addAttribute("now", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    model.addAttribute("connectorTypes", Arrays.asList(ConnectorType.values()));
    return BASE_URL_TEMPLATE + TEMPLATE_IMPORT;
  }

  /**
   * Just to test the import via browser.
   *
   * @param fromDate fromDate
   * @param toDate toDate
   * @return template
   */
  @RequestMapping(value = PATH_IMPORT, params = {"import"}, method = RequestMethod.POST)
  public String importDocuments(final String fromDate, final String toDate, final String type,
      final RedirectAttributes redirectAttrs) {
    JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.put(DocumentImportJob.JOB_DATA_FROM_DATE, fromDate);
    jobDataMap.put(DocumentImportJob.JOB_DATA_TO_DATE, toDate);
    jobDataMap.put(DocumentImportJob.JOB_DATA_CONNECTOR_TYPE, type);
    JobDetail jobDetail = JobBuilder.newJob().ofType(DocumentImportJob.class)
        .storeDurably()
        .withIdentity(UUID.randomUUID().toString(), "document-import-jobs")
        .usingJobData(jobDataMap)
        .build();
    Trigger trigger = TriggerBuilder.newTrigger()
        .forJob(jobDetail)
        .withIdentity(jobDetail.getKey().getName(), "document-import-triggers")
        .startNow()
        .build();
    try {
      scheduler.scheduleJob(jobDetail, trigger);
    } catch (SchedulerException e) {
      LOG.error("Failed to schedule document import", e);
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE,
          CODE_MESSAGE_SCHEDULE_DOCUMENTIMPORT_FAILED);
    }
    return "redirect:" + BASE_PATH;
  }

}
