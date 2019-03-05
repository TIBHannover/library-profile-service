package eu.tib.profileservice.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.DocumentMetadata;
import eu.tib.profileservice.service.DocumentImportService;
import eu.tib.profileservice.service.DocumentService;
import eu.tib.profileservice.service.UserService;
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
  private DocumentImportService documentImportService;
  @MockBean
  private DocumentService documentService;
  @MockBean
  private UserService userService;

  private Document newDocumentDummy() {
    final Document document = new Document();
    document.setId(1L);
    final DocumentMetadata documentMeta = new DocumentMetadata();
    documentMeta.setTitle("title");
    documentMeta.setDeweyDecimalClassifications(new HashSet<String>(Arrays.asList(new String[] {
        "300"})));
    document.setMetadata(documentMeta);
    document.setCreationDateUtc(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());
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
    when(documentService.findAllByExample(Mockito.any(Document.class), Mockito.any(Pageable.class)))
        .thenReturn(new PageImpl<Document>(Arrays.asList(new Document[] {document})));
    mvc.perform(get(DocumentController.BASE_PATH + DocumentController.PATH_LIST))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(document.getMetadata().getTitle())));
  }

  // TODO vervollständigen

}
