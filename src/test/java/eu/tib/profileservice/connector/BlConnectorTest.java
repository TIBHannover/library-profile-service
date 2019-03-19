package eu.tib.profileservice.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
public class BlConnectorTest {

  @MockBean
  private RestTemplate restTemplateMock;

  private BlConnector connector;

  /**
   * Setup.
   */
  @Before
  public void setup() {
    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    connector = new BlConnector(restTemplateMock, "http://some.url", now, now);
  }

  private void expectResourceAsRestTemplateRespone(final String resourceName) throws IOException {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
      final byte[] responseBody = IOUtils.toByteArray(is);
      ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(responseBody, HttpStatus.OK);
      when(restTemplateMock.getForEntity(Mockito.anyString(), ArgumentMatchers
          .<Class<byte[]>>any())).thenReturn(response);
    }
  }

  @Test
  public void testDetermineRdfFiles() {
    LocalDate from = LocalDate.parse("2019-03-13");
    LocalDate to = LocalDate.parse("2019-03-13");
    List<String> result = connector.determineRdfFilesForDateRange(from, to);
    assertThat(result).containsExactly("bnbrdf_N3536.zip");

    from = LocalDate.parse("2019-03-12");
    to = LocalDate.parse("2019-03-13");
    result = connector.determineRdfFilesForDateRange(from, to);
    assertThat(result).containsExactly("bnbrdf_N3535.zip", "bnbrdf_N3536.zip");

    from = LocalDate.parse("2019-03-13");
    to = LocalDate.parse("2019-03-19");
    result = connector.determineRdfFilesForDateRange(from, to);
    assertThat(result).containsExactly("bnbrdf_N3536.zip");

    from = LocalDate.parse("2019-03-01");
    to = LocalDate.parse("2019-03-15");
    result = connector.determineRdfFilesForDateRange(from, to);
    assertThat(result).containsExactly("bnbrdf_N3534.zip", "bnbrdf_N3535.zip", "bnbrdf_N3536.zip");

    from = LocalDate.parse("2019-03-14");
    to = LocalDate.parse("2019-03-13");
    result = connector.determineRdfFilesForDateRange(from, to);
    assertThat(result).isEmpty();
  }

  @Test
  public void testNoRequest() {
    LocalDate from = LocalDate.parse("2019-03-18");
    LocalDate to = LocalDate.parse("2019-03-15");
    connector = new BlConnector(restTemplateMock, "http://some.url", from, to);
    assertFalse(connector.hasNext());
  }

  @Test
  public void testRetrieveDocumentsFailsWithNotOkStatus() {
    ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
    when(restTemplateMock.getForEntity(Mockito.anyString(), ArgumentMatchers.<Class<byte[]>>any()))
        .thenReturn(response);

    List<DocumentMetadata> result = connector.retrieveNextDocuments();
    assertTrue(result == null || result.isEmpty());
    assertTrue(connector.hasErrors());
    assertFalse(connector.hasNext());
  }

  @Test
  public void testRetrieveDocumentsFailsWithoutBody() {
    ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(HttpStatus.OK);
    when(restTemplateMock.getForEntity(Mockito.anyString(), ArgumentMatchers.<Class<byte[]>>any()))
        .thenReturn(response);

    List<DocumentMetadata> result = connector.retrieveNextDocuments();
    assertTrue(result == null || result.isEmpty());
    assertTrue(connector.hasErrors());
  }

  @Test
  public void testRetrieveDocumentsFailsWithRestClientException() {
    when(restTemplateMock.getForEntity(Mockito.anyString(), ArgumentMatchers.<Class<byte[]>>any()))
        .thenThrow(RestClientException.class);

    List<DocumentMetadata> result = connector.retrieveNextDocuments();
    assertTrue(result == null || result.isEmpty());
    assertTrue(connector.hasErrors());
  }

  @Test
  public void testRetrieveDocumentsFailsWithEmptyZipFile() {
    ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(new byte[] {0, 0, 0, 0},
        HttpStatus.OK);
    when(restTemplateMock.getForEntity(Mockito.anyString(), ArgumentMatchers.<Class<byte[]>>any()))
        .thenReturn(response);

    List<DocumentMetadata> result = connector.retrieveNextDocuments();
    assertTrue(result == null || result.isEmpty());
    assertTrue(connector.hasErrors());
  }

  @Test
  public void testRetrieveDocumentsFailsWithInvalidZipEntry() throws IOException {
    expectResourceAsRestTemplateRespone("connector/BLResponse001.zip");

    List<DocumentMetadata> result = connector.retrieveNextDocuments();
    assertTrue(result == null || result.isEmpty());
    assertTrue(connector.hasErrors());
  }

  @Test
  public void testRetrieveDocumentsFailsWithCorruptZipFile() throws IOException {
    expectResourceAsRestTemplateRespone("connector/BLResponse002.zip");

    List<DocumentMetadata> result = connector.retrieveNextDocuments();
    assertTrue(result == null || result.isEmpty());
    assertTrue(connector.hasErrors());
  }

  @Test
  public void testRetrieveDocumentsOk() throws IOException {
    expectResourceAsRestTemplateRespone("connector/BLResponse003.zip");

    List<DocumentMetadata> result = connector.retrieveNextDocuments();
    assertThat(result).isNotNull();
    assertThat(result.size()).isEqualTo(1);
    assertFalse(connector.hasErrors());
  }

  @Test
  public void testMultipleRequests() throws IOException {
    LocalDate from = LocalDate.parse("2019-03-01");
    LocalDate to = LocalDate.parse("2019-03-15");
    connector = new BlConnector(restTemplateMock, "http://some.url", from, to);
    expectResourceAsRestTemplateRespone("connector/BLResponse003.zip");

    List<DocumentMetadata> result;
    assertTrue(connector.hasNext());
    result = connector.retrieveNextDocuments();
    assertThat(result).isNotNull();
    assertThat(result.size()).isEqualTo(1);
    assertTrue(connector.hasNext());
    result = connector.retrieveNextDocuments();
    assertThat(result).isNotNull();
    assertThat(result.size()).isEqualTo(1);
    assertTrue(connector.hasNext());
    result = connector.retrieveNextDocuments();
    assertThat(result).isNotNull();
    assertThat(result.size()).isEqualTo(1);
    assertFalse(connector.hasNext());
  }

}
