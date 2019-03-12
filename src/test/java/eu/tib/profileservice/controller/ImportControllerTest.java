package eu.tib.profileservice.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.tib.profileservice.domain.ImportFilter;
import eu.tib.profileservice.domain.ImportFilter.Action;
import eu.tib.profileservice.domain.ImportFilter.ConditionType;
import eu.tib.profileservice.service.ImportFilterService;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test for {@link ImportController}.
 */
@RunWith(SpringRunner.class)
@WebMvcTest(ImportController.class)
public class ImportControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private ImportFilterService importFilterService;
  @MockBean
  private Scheduler scheduler;

  private ImportFilter newFilter(final Action action, final String condition,
      final ConditionType conditionType) {
    ImportFilter filter = new ImportFilter();
    filter.setId(1L);
    filter.setAction(action);
    filter.setCondition(condition);
    filter.setConditionType(conditionType);
    return filter;
  }

  @Test
  public void testIndex() throws Exception {
    mvc.perform(get(ImportController.BASE_PATH))
        .andExpect(redirectedUrl(ImportController.BASE_PATH + ImportController.PATH_LISTFILTER));
  }

  @Test
  public void testList() throws Exception {
    ImportFilter filter = newFilter(Action.IGNORE, "test regular expression",
        ConditionType.FORM_KEYWORD);
    when(importFilterService.findAll()).thenReturn(Arrays.asList(new ImportFilter[] {filter}));
    mvc.perform(get(ImportController.BASE_PATH + ImportController.PATH_LISTFILTER))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(filter.getCondition())));
  }

  @Test
  public void testShowFilter() throws Exception {
    ImportFilter filter = newFilter(Action.IGNORE, "test regular expression",
        ConditionType.FORM_KEYWORD);
    when(importFilterService.findById(1L)).thenReturn(filter);

    mvc.perform(get(ImportController.BASE_PATH + ImportController.PATH_SHOWFILTER + "/{id}/", filter
        .getId()))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(filter.getCondition())));

    when(importFilterService.findById(2L)).thenReturn(null);
    mvc.perform(get(ImportController.BASE_PATH + ImportController.PATH_SHOWFILTER + "/{id}/", 2L))
        .andExpect(redirectedUrl(ImportController.BASE_PATH + ImportController.PATH_LISTFILTER))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_ERROR))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE,
            ImportController.CODE_MESSAGE_FILTER_NOTFOUND));
  }

  @Test
  public void testCreateFilter() throws Exception {
    mvc.perform(get(ImportController.BASE_PATH + ImportController.PATH_CREATEFILTER))
        .andExpect(status().isOk());
  }

  @Test
  public void testEditFilter() throws Exception {
    ImportFilter filter = newFilter(Action.IGNORE, "test regular expression",
        ConditionType.FORM_KEYWORD);
    when(importFilterService.findById(1L)).thenReturn(filter);

    mvc.perform(get(ImportController.BASE_PATH + ImportController.PATH_EDITFILTER + "/{id}/", filter
        .getId()))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(filter.getCondition())));

    when(importFilterService.findById(2L)).thenReturn(null);
    mvc.perform(get(ImportController.BASE_PATH + ImportController.PATH_EDITFILTER + "/{id}/", 2L))
        .andExpect(redirectedUrl(ImportController.BASE_PATH + ImportController.PATH_LISTFILTER))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_ERROR))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE,
            ImportController.CODE_MESSAGE_FILTER_NOTFOUND));
  }

  @Test
  public void testSaveFilter() throws Exception {
    ImportFilter filter = newFilter(Action.IGNORE, "test regular expression",
        ConditionType.FORM_KEYWORD);

    mvc.perform(post(ImportController.BASE_PATH + ImportController.PATH_SAVE_FILTER, filter).with(
        csrf()))
        .andExpect(redirectedUrl(ImportController.BASE_PATH + ImportController.PATH_LISTFILTER));

    verify(importFilterService, times(1)).createOrUpdate(Mockito.any(ImportFilter.class));
  }

  @Test
  public void testDeleteFilter() throws Exception {
    ImportFilter filter = newFilter(Action.IGNORE, "test regular expression",
        ConditionType.FORM_KEYWORD);

    mvc.perform(post(ImportController.BASE_PATH + ImportController.PATH_DELETE_FILTER, filter).with(
        csrf()))
        .andExpect(redirectedUrl(ImportController.BASE_PATH + ImportController.PATH_LISTFILTER));

    verify(importFilterService, times(1)).delete(Mockito.any(ImportFilter.class));
  }

  @Test
  @WithMockUser(authorities = {"IMPORT_DOCUMENTS"})
  public void testImportDocumentsTemplate() throws Exception {
    mvc.perform(get(ImportController.BASE_PATH + ImportController.PATH_IMPORT))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("import")));
  }

  @Test
  @WithMockUser(authorities = {"IMPORT_DOCUMENTS"})
  public void testImportDocuments() throws Exception {
    mvc.perform(post(ImportController.BASE_PATH + ImportController.PATH_IMPORT).param("fromDate",
        "2019-03-05").param("toDate", "2019-03-05").param("import", "import").with(csrf()))
        .andExpect(redirectedUrl(ImportController.BASE_PATH));

    verify(scheduler, times(1)).scheduleJob(Mockito.any(JobDetail.class), Mockito.any(
        Trigger.class));

    when(scheduler.scheduleJob(Mockito.any(JobDetail.class), Mockito.any(Trigger.class))).thenThrow(
        SchedulerException.class);
    mvc.perform(post(ImportController.BASE_PATH + ImportController.PATH_IMPORT).param("fromDate",
        "2019-03-05").param("toDate", "2019-03-05").param("import", "import").with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_ERROR))
        .andExpect(redirectedUrl(ImportController.BASE_PATH));
  }
}
