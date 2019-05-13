package eu.tib.profileservice.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.DocumentMetadata;
import eu.tib.profileservice.domain.DocumentSearch;
import eu.tib.profileservice.domain.User;
import eu.tib.profileservice.service.DocumentService;
import eu.tib.profileservice.service.UserService;
import eu.tib.profileservice.util.FileExportProcessor;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(DocumentController.class)
@WithMockUser(authorities = {"PROCESS_DOCUMENTS"})
public class DocumentControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private DocumentService documentService;
  @MockBean
  private UserService userService;
  @MockBean
  private FileExportProcessor fileExportProcessor;

  private Document newDocumentDummy() {
    final Document document = new Document();
    document.setId(1L);
    final DocumentMetadata documentMeta = new DocumentMetadata();
    documentMeta.setTitle("title");
    documentMeta.setDeweyDecimalClassifications(new HashSet<String>(Arrays.asList(new String[] {
        "300"})));
    document.setMetadata(documentMeta);
    document.setCreationDateUtc(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    document.setExpiryDateUtc(document.getCreationDateUtc());
    return document;
  }

  @Test
  public void testIndex() throws Exception {
    mvc.perform(get(DocumentController.BASE_PATH))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_MY));
  }

  @Test
  public void testList() throws Exception {
    Document document = newDocumentDummy();
    when(documentService.findAllByDocumentSearch(Mockito.any(DocumentSearch.class), Mockito.any(
        Pageable.class)))
            .thenReturn(new PageImpl<Document>(Arrays.asList(new Document[] {document})));
    mvc.perform(get(DocumentController.BASE_PATH + DocumentController.PATH_LIST))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(document.getMetadata().getTitle())));
  }

  @Test
  public void testMyList() throws Exception {
    User user = new User();
    user.setId(1L);
    user.setName("name");
    when(userService.findByName(Mockito.anyString())).thenReturn(user);
    mvc.perform(get(DocumentController.BASE_PATH + DocumentController.PATH_MY))
        .andExpect(redirectedUrlPattern(DocumentController.BASE_PATH + DocumentController.PATH_LIST
            + "*"));

    when(userService.findByName(Mockito.anyString())).thenReturn(null);
    mvc.perform(get(DocumentController.BASE_PATH + DocumentController.PATH_MY))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_LIST));
  }

  @Test
  public void testAcceptDocument() throws Exception {
    when(documentService.acceptDocument(Mockito.anyLong())).thenReturn(null);

    mvc.perform(post(DocumentController.BASE_PATH + DocumentController.PATH_UPDATE)
        .param(DocumentController.METHOD_ACCEPT, DocumentController.METHOD_ACCEPT)
        .param("documents[0].id", "1")
        .param("selected[0]", "true")
        .param("sourceUri", DocumentController.BASE_PATH + DocumentController.PATH_LIST)
        .param("sourceQuery", "")
        .with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_ERROR))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_LIST));
    verify(documentService, times(1)).acceptDocument(1L);

    reset(documentService);
    when(documentService.acceptDocument(Mockito.anyLong())).thenReturn(newDocumentDummy());
    mvc.perform(post(DocumentController.BASE_PATH + DocumentController.PATH_UPDATE)
        .param(DocumentController.METHOD_ACCEPT, DocumentController.METHOD_ACCEPT)
        .param("documents[0].id", "1")
        .param("selected[0]", "true")
        .param("documents[1].id", "2")
        .param("selected[1]", "false")
        .param("documents[2].assignee.id", "1")
        .param("selected[2]", "true")
        .param("documents[3].assignee.id", "1")
        .param("selected[3]", "false")
        .param("sourceUri", DocumentController.BASE_PATH + DocumentController.PATH_LIST)
        .param("sourceQuery", "")
        .with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_SUCCESS))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_LIST));
    verify(documentService, times(1)).acceptDocument(1L);

    reset(documentService);
    mvc.perform(post(DocumentController.BASE_PATH + DocumentController.PATH_UPDATE)
        .param(DocumentController.METHOD_ACCEPT, DocumentController.METHOD_ACCEPT)
        .param("documents[0].id", "1")
        .param("sourceUri", DocumentController.BASE_PATH + DocumentController.PATH_LIST)
        .param("sourceQuery", "assignee=1")
        .with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_ERROR))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_LIST
            + "?assignee=1"));
    verify(documentService, times(0)).acceptDocument(Mockito.anyLong());

    reset(documentService);
    mvc.perform(post(DocumentController.BASE_PATH + DocumentController.PATH_UPDATE)
        .param(DocumentController.METHOD_ACCEPT, DocumentController.METHOD_ACCEPT)
        .param("selected[0]", "true")
        .param("sourceUri", DocumentController.BASE_PATH + DocumentController.PATH_LIST)
        .param("sourceQuery", "")
        .with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_ERROR))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_LIST));
    verify(documentService, times(0)).acceptDocument(Mockito.anyLong());

    reset(documentService);
    mvc.perform(post(DocumentController.BASE_PATH + DocumentController.PATH_UPDATE)
        .param(DocumentController.METHOD_ACCEPT, DocumentController.METHOD_ACCEPT)
        .param("sourceUri", DocumentController.BASE_PATH + DocumentController.PATH_LIST)
        .param("sourceQuery", "")
        .with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_ERROR))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_LIST));
    verify(documentService, times(0)).acceptDocument(Mockito.anyLong());
  }

  @Test
  public void testRejectDocument() throws Exception {
    when(documentService.rejectDocument(Mockito.anyLong())).thenReturn(null);

    mvc.perform(post(DocumentController.BASE_PATH + DocumentController.PATH_UPDATE)
        .param(DocumentController.METHOD_REJECT, DocumentController.METHOD_REJECT)
        .param("documents[0].id", "1")
        .param("selected[0]", "true")
        .param("sourceUri", DocumentController.BASE_PATH + DocumentController.PATH_LIST)
        .param("sourceQuery", "")
        .with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_ERROR))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_LIST));
    verify(documentService, times(1)).rejectDocument(1L);

    reset(documentService);
    when(documentService.rejectDocument(Mockito.anyLong())).thenReturn(newDocumentDummy());
    mvc.perform(post(DocumentController.BASE_PATH + DocumentController.PATH_UPDATE)
        .param(DocumentController.METHOD_REJECT, DocumentController.METHOD_REJECT)
        .param("documents[0].id", "1")
        .param("selected[0]", "true")
        .param("documents[1].id", "2")
        .param("selected[1]", "false")
        .param("documents[2].assignee.id", "1")
        .param("selected[2]", "true")
        .param("documents[3].assignee.id", "1")
        .param("selected[3]", "false")
        .param("sourceUri", DocumentController.BASE_PATH + DocumentController.PATH_LIST)
        .param("sourceQuery", "")
        .with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_SUCCESS))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_LIST));
    verify(documentService, times(1)).rejectDocument(1L);

    reset(documentService);
    mvc.perform(post(DocumentController.BASE_PATH + DocumentController.PATH_UPDATE)
        .param(DocumentController.METHOD_REJECT, DocumentController.METHOD_REJECT)
        .param("documents[0].id", "1")
        .param("sourceUri", DocumentController.BASE_PATH + DocumentController.PATH_LIST)
        .param("sourceQuery", "assignee=1")
        .with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_ERROR))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_LIST
            + "?assignee=1"));
    verify(documentService, times(0)).acceptDocument(Mockito.anyLong());

    reset(documentService);
    mvc.perform(post(DocumentController.BASE_PATH + DocumentController.PATH_UPDATE)
        .param(DocumentController.METHOD_REJECT, DocumentController.METHOD_REJECT)
        .param("selected[0]", "true")
        .param("sourceUri", DocumentController.BASE_PATH + DocumentController.PATH_LIST)
        .param("sourceQuery", "")
        .with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_ERROR))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_LIST));
    verify(documentService, times(0)).acceptDocument(Mockito.anyLong());

    reset(documentService);
    mvc.perform(post(DocumentController.BASE_PATH + DocumentController.PATH_UPDATE)
        .param(DocumentController.METHOD_REJECT, DocumentController.METHOD_REJECT)
        .param("sourceUri", DocumentController.BASE_PATH + DocumentController.PATH_LIST)
        .param("sourceQuery", "assignee=1")
        .with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_ERROR))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_LIST
            + "?assignee=1"));
    verify(documentService, times(0)).rejectDocument(Mockito.anyLong());
  }

  @Test
  public void testAssignDocument() throws Exception {
    when(documentService.assignToUser(Mockito.any(Document.class), Mockito.anyLong())).thenReturn(
        null);

    mvc.perform(post(DocumentController.BASE_PATH + DocumentController.PATH_UPDATE)
        .param(DocumentController.METHOD_ASSIGN, DocumentController.METHOD_ASSIGN)
        .param("documents[0].id", "1")
        .param("selected[0]", "true")
        .param("assigneeId", "1")
        .param("sourceUri", DocumentController.BASE_PATH + DocumentController.PATH_LIST)
        .param("sourceQuery", "")
        .with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_ERROR))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_LIST));
    verify(documentService, times(1)).assignToUser(Mockito.any(Document.class), Mockito.anyLong());

    reset(documentService);
    Document document = newDocumentDummy();
    document.setAssignee(new User());
    when(documentService.assignToUser(Mockito.any(Document.class), Mockito.anyLong())).thenReturn(
        document);
    mvc.perform(post(DocumentController.BASE_PATH + DocumentController.PATH_UPDATE)
        .param(DocumentController.METHOD_ASSIGN, DocumentController.METHOD_ASSIGN)
        .param("documents[0].id", "1")
        .param("selected[0]", "true")
        .param("assigneeId", "1")
        .param("sourceUri", DocumentController.BASE_PATH + DocumentController.PATH_LIST)
        .param("sourceQuery", "")
        .with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_SUCCESS))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_LIST));
    verify(documentService, times(1)).assignToUser(Mockito.any(Document.class), Mockito.anyLong());

    reset(documentService);
    mvc.perform(post(DocumentController.BASE_PATH + DocumentController.PATH_UPDATE)
        .param(DocumentController.METHOD_ASSIGN, DocumentController.METHOD_ASSIGN)
        .param("assigneeId", "1")
        .param("sourceUri", DocumentController.BASE_PATH + DocumentController.PATH_LIST)
        .with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_ERROR))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_LIST));
    verify(documentService, times(0)).assignToUser(Mockito.any(Document.class), Mockito.anyLong());

    reset(documentService);
    mvc.perform(post(DocumentController.BASE_PATH + DocumentController.PATH_UPDATE)
        .param(DocumentController.METHOD_ASSIGN, DocumentController.METHOD_ASSIGN)
        .param("documents[0].id", "1")
        .param("selected[0]", "true")
        .param("sourceUri", DocumentController.BASE_PATH + DocumentController.PATH_LIST)
        .with(csrf()))
        .andExpect(status().isBadRequest());
    verify(documentService, times(0)).assignToUser(Mockito.any(Document.class), Mockito.anyLong());
  }

  @Test
  public void testSetDocumentToPending() throws Exception {
    when(documentService.findById(Mockito.anyLong())).thenReturn(newDocumentDummy());
    when(documentService.saveDocument(Mockito.any(Document.class))).thenReturn(newDocumentDummy());

    mvc.perform(post(DocumentController.BASE_PATH + DocumentController.PATH_UPDATE)
        .param(DocumentController.METHOD_PENDING, DocumentController.METHOD_PENDING)
        .param("documents[0].id", "1")
        .param("selected[0]", "true")
        .param("newExpiryDate", "2019-04-17")
        .param("sourceUri", DocumentController.BASE_PATH + DocumentController.PATH_LIST)
        .param("sourceQuery", "")
        .with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_SUCCESS))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_LIST));
    verify(documentService, times(1)).saveDocument(Mockito.any(Document.class));

    reset(documentService);
    when(documentService.findById(Mockito.anyLong())).thenReturn(newDocumentDummy());
    when(documentService.saveDocument(Mockito.any(Document.class))).thenReturn(newDocumentDummy());
    mvc.perform(post(DocumentController.BASE_PATH + DocumentController.PATH_UPDATE)
        .param(DocumentController.METHOD_PENDING, DocumentController.METHOD_PENDING)
        .param("documents[0].id", "1")
        .param("selected[0]", "true")
        .param("sourceUri", DocumentController.BASE_PATH + DocumentController.PATH_LIST)
        .param("sourceQuery", "")
        .with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_SUCCESS))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_LIST));
    verify(documentService, times(1)).saveDocument(Mockito.any(Document.class));

    reset(documentService);
    when(documentService.findById(Mockito.anyLong())).thenReturn(null);
    mvc.perform(post(DocumentController.BASE_PATH + DocumentController.PATH_UPDATE)
        .param(DocumentController.METHOD_PENDING, DocumentController.METHOD_PENDING)
        .param("documents[0].id", "1")
        .param("selected[0]", "true")
        .param("sourceUri", DocumentController.BASE_PATH + DocumentController.PATH_LIST)
        .param("sourceQuery", "assignee=1")
        .with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_ERROR))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_LIST
            + "?assignee=1"));
    verify(documentService, times(0)).saveDocument(Mockito.any(Document.class));

    reset(documentService);
    mvc.perform(post(DocumentController.BASE_PATH + DocumentController.PATH_UPDATE)
        .param(DocumentController.METHOD_PENDING, DocumentController.METHOD_PENDING)
        .param("sourceUri", DocumentController.BASE_PATH + DocumentController.PATH_LIST)
        .param("sourceQuery", "assignee=1")
        .with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_ERROR))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_LIST
            + "?assignee=1"));
    verify(documentService, times(0)).saveDocument(Mockito.any(Document.class));
  }

  @Test
  public void testExport() throws Exception {
    long count = 1L;
    when(documentService.countByExample(Mockito.any(Document.class))).thenReturn(count);
    mvc.perform(get(DocumentController.BASE_PATH + DocumentController.PATH_EXPORT))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(String.valueOf(count))));
  }

  @Test
  public void testProcessExport() throws Exception {
    when(documentService.export()).thenReturn(false);
    mvc.perform(get(DocumentController.BASE_PATH + DocumentController.PATH_EXPORT)
        .param(DocumentController.METHOD_EXPORT, DocumentController.METHOD_EXPORT)
        .with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_ERROR))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_EXPORT));

    when(documentService.export()).thenReturn(true);
    mvc.perform(get(DocumentController.BASE_PATH + DocumentController.PATH_EXPORT)
        .param(DocumentController.METHOD_EXPORT, DocumentController.METHOD_EXPORT)
        .with(csrf()))
        .andExpect(flash().attribute(HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE,
            HomeController.INFO_MESSAGE_TYPE_SUCCESS))
        .andExpect(redirectedUrl(DocumentController.BASE_PATH + DocumentController.PATH_EXPORT));
  }

  @Test
  public void testDownload() throws Exception {
    when(fileExportProcessor.getBytesOfExportFile(Mockito.anyString())).thenReturn(null);
    mvc.perform(get(DocumentController.BASE_PATH + DocumentController.PATH_EXPORT)
        .param(DocumentController.METHOD_DOWNLOAD, DocumentController.METHOD_DOWNLOAD)
        .param("file", "xxxx")
        .with(csrf()))
        .andExpect(status().isNoContent());

    byte[] dummy = new byte[] {0, 0, 0, 0, 0, 0, 0, 0};
    when(fileExportProcessor.getBytesOfExportFile(Mockito.anyString())).thenReturn(dummy);
    mvc.perform(get(DocumentController.BASE_PATH + DocumentController.PATH_EXPORT)
        .param(DocumentController.METHOD_DOWNLOAD, DocumentController.METHOD_DOWNLOAD)
        .param("file", "xxxx")
        .with(csrf()))
        .andExpect(status().isOk());

  }

}
