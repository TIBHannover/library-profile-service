package eu.tib.profileservice.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import eu.tib.profileservice.domain.DocumentMetadata;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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
@ContextConfiguration(classes = TibConnector.class)
@TestPropertySource(value = "classpath:application.properties")
public class TibConnectorTest {

  @TestConfiguration
  static class TestContextConfiguration {

    @Bean
    public InventoryConnector connector() {
      return new TibConnector();
    }
  }

  @Autowired
  private InventoryConnector connector;

  @MockBean
  private RestTemplate restTemplateMock;

  private DocumentMetadata newDocumentMetadataDummy(final String... isbns) {
    final DocumentMetadata document = new DocumentMetadata();
    document.setTitle("title");
    document.setRemainderOfTitle("remainderOfTitle");
    document.setIsbns(Arrays.asList(isbns));
    return document;
  }

  @Test(expected = ConnectorException.class)
  public void testRetrieveDocumentsFails() throws ConnectorException {
    ResponseEntity<String> response = new ResponseEntity<String>("", HttpStatus.NOT_FOUND);
    when(restTemplateMock.getForEntity(Mockito.anyString(), ArgumentMatchers.<Class<String>>any()))
        .thenReturn(response);

    connector.processInventoryCheck(newDocumentMetadataDummy("123456789"));
  }

  @Test(expected = ConnectorException.class)
  public void testRetrieveInvalidDocuments() throws ConnectorException {
    ResponseEntity<String> response = new ResponseEntity<String>("invalid records", HttpStatus.OK);
    when(restTemplateMock.getForEntity(Mockito.anyString(), ArgumentMatchers.<Class<String>>any()))
        .thenReturn(response);

    connector.processInventoryCheck(newDocumentMetadataDummy("123456789", "987654321"));
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
  public void testContains() throws IOException, ConnectorException {
    expectResourceAsRestTemplateRespone("connector/TIBResponse001.xml");
    DocumentMetadata metadata = newDocumentMetadataDummy("123456789", "987654321");
    boolean contains = connector.processInventoryCheck(metadata);
    assertTrue(contains);
    assertThat(metadata.getInventoryUri()).isEqualTo(
        "https://www.tib.eu/de/suchen/id/TIBKAT%3A772916411");

    expectResourceAsRestTemplateRespone("connector/TIBResponse002.xml");
    contains = connector.processInventoryCheck(newDocumentMetadataDummy("123456789", "987654321"));
    assertFalse(contains);
  }

}
