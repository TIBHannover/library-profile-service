package eu.tib.profileservice.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import eu.tib.profileservice.domain.DocumentMetadata;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = DnbConnector.class)
@TestPropertySource(value = "classpath:application.properties")
public class DnbConnectorTest {

  @TestConfiguration
  static class TestContextConfiguration {

    @Bean
    public DnbConnector dnbConnector() {
      return new DnbConnector();
    }
  }

  @MockBean
  private RestTemplate restTemplateMock;

  @Autowired
  private DnbConnector conn;

  @Test
  public void testRetrieveDocumentsFails() {
    ResponseEntity<String> response = new ResponseEntity<String>("", HttpStatus.NOT_FOUND);
    when(restTemplateMock.getForEntity(Mockito.anyString(), ArgumentMatchers.<Class<String>>any()))
        .thenReturn(response);

    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    List<DocumentMetadata> result = conn.retrieveDocuments(now, now);
    assertTrue(result == null || result.isEmpty());
  }

  @Test
  public void testRetrieveInvalidDocuments() {
    ResponseEntity<String> response = new ResponseEntity<String>("invalid records", HttpStatus.OK);
    when(restTemplateMock.getForEntity(Mockito.anyString(), ArgumentMatchers.<Class<String>>any()))
        .thenReturn(response);

    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    List<DocumentMetadata> result = conn.retrieveDocuments(now, now);
    assertTrue(result == null || result.isEmpty());
  }

  @Test
  public void testRetrieveDocuments() throws IOException {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(
        "connector/DNBResponse001.xml")) {
      final String responseBody = IOUtils.toString(is);
      ResponseEntity<String> response = new ResponseEntity<String>(responseBody, HttpStatus.OK);
      when(restTemplateMock.getForEntity(Mockito.anyString(), ArgumentMatchers
          .<Class<String>>any())).thenReturn(response);
    }

    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    List<DocumentMetadata> result = conn.retrieveDocuments(now, now);
    assertNotNull(result);
    assertEquals(2, result.size());
  }

}
