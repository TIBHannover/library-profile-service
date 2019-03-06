package eu.tib.profileservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.Document.Status;
import eu.tib.profileservice.domain.DocumentMetadata;
import eu.tib.profileservice.domain.User;
import eu.tib.profileservice.repository.DocumentRepository;
import eu.tib.profileservice.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class DocumentServiceTest {

  @TestConfiguration
  static class TestContextConfiguration {

    @Bean
    public DocumentService service() {
      return new DocumentServiceImpl();
    }
  }

  @Autowired
  private DocumentService documentService;
  @MockBean
  private DocumentRepository documentRepository;
  @MockBean
  private UserRepository userRepository;

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

  private User newUserDummy() {
    final User user = new User();
    user.setId(1L);
    user.setName("name");
    user.setPassword("password");
    user.setInitials("initials");
    return user;
  }

  @Test
  public void testFindAllByExample() {
    when(documentRepository.findAll(ArgumentMatchers.<Example<Document>>any(), Mockito.any(
        Pageable.class)))
            .thenReturn(new PageImpl<Document>(Arrays.asList(new Document[] {newDocumentDummy()})));
    documentService.findAllByExample(new Document(), null);
  }

  @Test
  public void testFindById() {
    when(documentRepository.findById(1L)).thenReturn(Optional.of(newDocumentDummy()));

    Document result = documentService.findById(null);
    assertThat(result).isNull();

    result = documentService.findById(1L);
    assertThat(result).isNotNull();

    when(documentRepository.findById(1L)).thenThrow(NoSuchElementException.class);
    result = documentService.findById(1L);
    assertThat(result).isNull();
  }

  @Test
  public void testAssignToUser() {
    Document document = newDocumentDummy();
    User user = newUserDummy();

    when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
    when(userRepository.getOne(1L)).thenReturn(null);
    when(documentRepository.save(document)).thenReturn(document);

    Document result = documentService.assignToUser(null, user);
    assertThat(result).isNull();
    result = documentService.assignToUser(document, null);
    assertThat(result).isNull();
    document.setId(null);
    result = documentService.assignToUser(document, user);
    assertThat(result).isNull();
    document.setId(1L);
    user.setId(null);
    result = documentService.assignToUser(document, user);
    assertThat(result).isNull();
    user.setId(1L);
    result = documentService.assignToUser(document, user);
    assertThat(result).isNull();

    when(userRepository.getOne(1L)).thenReturn(user);
    result = documentService.assignToUser(document, user);
    assertThat(result).isNotNull();
    assertThat(result.getAssignee()).isNotNull();
  }

  @Test
  public void testAcceptDocument() {
    Document document = newDocumentDummy();
    when(documentRepository.getOne(1L)).thenReturn(document);
    when(documentRepository.save(document)).thenReturn(document);

    Document result = documentService.acceptDocument(null);
    assertThat(result).isNull();

    result = documentService.acceptDocument(1L);
    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(Status.ACCEPTED);

    verify(documentRepository, times(1)).save(Mockito.any(Document.class));
  }

  @Test
  public void testRejectDocument() {
    Document document = newDocumentDummy();
    when(documentRepository.getOne(1L)).thenReturn(document);
    when(documentRepository.save(document)).thenReturn(document);

    Document result = documentService.rejectDocument(null);
    assertThat(result).isNull();

    result = documentService.rejectDocument(1L);
    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(Status.REJECTED);

    verify(documentRepository, times(1)).save(Mockito.any(Document.class));
  }

  @Test
  public void testDeleteDocumentsBeforeExpiryDate() {
    documentService.deleteDocumentCreatedBefore(OffsetDateTime.now(ZoneOffset.UTC)
        .toLocalDateTime());
    verify(documentRepository, times(1)).deleteByCreationDateUtcBefore(Mockito.any(
        LocalDateTime.class));
  }

}
