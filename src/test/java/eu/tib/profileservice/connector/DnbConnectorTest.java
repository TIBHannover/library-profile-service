package eu.tib.profileservice.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
public class DnbConnectorTest {

  @MockBean
  private RestTemplate restTemplateMock;

  private DnbConnector conn;

  /**
   * Setup.
   */
  @Before
  public void setup() {
    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    conn = new DnbConnector(restTemplateMock, "http://some.url", "token", now, now);
  }

  @Test
  public void testRetrieveDocumentsFails() {
    ResponseEntity<String> response = new ResponseEntity<String>("", HttpStatus.NOT_FOUND);
    when(restTemplateMock.getForEntity(Mockito.anyString(), ArgumentMatchers.<Class<String>>any()))
        .thenReturn(response);

    List<DocumentMetadata> result = conn.retrieveNextDocuments();
    assertTrue(result == null || result.isEmpty());
  }

  @Test
  public void testRetrieveInvalidDocuments() {
    ResponseEntity<String> response = new ResponseEntity<String>("invalid records", HttpStatus.OK);
    when(restTemplateMock.getForEntity(Mockito.anyString(), ArgumentMatchers.<Class<String>>any()))
        .thenReturn(response);

    List<DocumentMetadata> result = conn.retrieveNextDocuments();
    assertTrue(result == null || result.isEmpty());
  }

  private void expectResourceAsRestTemplateRespone(final String resourceName) throws IOException {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
      final String responseBody = IOUtils.toString(is);
      ResponseEntity<String> response = new ResponseEntity<String>(responseBody, HttpStatus.OK);
      when(restTemplateMock.getForEntity(Mockito.anyString(), ArgumentMatchers
          .<Class<String>>any())).thenReturn(response);
    }

  }

  @Test
  public void testRetrieveDocuments() throws IOException {
    expectResourceAsRestTemplateRespone("connector/DNBResponse001.xml");

    List<DocumentMetadata> result = conn.retrieveNextDocuments();
    assertNotNull(result);
    assertEquals(2, result.size());
  }

  @Test
  public void testRetrieveDocumentsWithResumptionToken() throws IOException {
    expectResourceAsRestTemplateRespone("connector/DNBResponse002.xml");

    List<DocumentMetadata> result = conn.retrieveNextDocuments();
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(conn.hasNext());

    expectResourceAsRestTemplateRespone("connector/DNBResponse003.xml");
    result = conn.retrieveNextDocuments();
    assertNotNull(result);
    assertEquals(2, result.size());
    assertFalse(conn.hasNext());
  }

  @Test
  public void testRetrieveDocumentsWithIsbnContainingText() throws IOException {
    expectResourceAsRestTemplateRespone("connector/DNBResponse004.xml");

    List<DocumentMetadata> result = conn.retrieveNextDocuments();
    assertNotNull(result);
    assertEquals(1, result.size());
    assertThat(result.get(0).getIsbns()).contains("9783658161262", "3658161264", "invalid");
  }

  @Test
  public void testRetrieveDocumentsWithoutMatchingBibNr() throws IOException {
    expectResourceAsRestTemplateRespone("connector/DNBResponse005.xml");
    List<DocumentMetadata> result = conn.retrieveNextDocuments();
    assertNotNull(result);
    assertEquals(1, result.size());
  }

}
