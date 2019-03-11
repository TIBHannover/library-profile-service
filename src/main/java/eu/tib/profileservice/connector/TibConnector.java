package eu.tib.profileservice.connector;

import eu.tib.profileservice.domain.DocumentMetadata;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Component
public class TibConnector implements InventoryConnector {

  private static final Logger LOG = LoggerFactory.getLogger(TibConnector.class);

  private static final String RESULT_FORMAT = "dcx";

  //  private static final String RESULT_FORMAT = "marcxml";
  //  private static final String MARCXML_RECORD_PATH =
  //      "/searchRetrieveResponse/records/record/recordData/record";

  private final String baseUrl = "https://getinfo.tib.eu/sru";

  @Autowired
  private RestTemplate restTemplate;

  private String buildRequest(final DocumentMetadata documentMetadata) {
    final StringBuilder sb = new StringBuilder();
    sb.append(baseUrl);
    sb.append("/all?version=2.0&operation=searchRetrieve&queryType=solr");
    sb.append("&recordSchema=").append(RESULT_FORMAT);
    sb.append("&query=prefix:tibkat");
    StringBuilder isbnQuery = new StringBuilder();
    for (String isbn : documentMetadata.getIsbns()) {
      if (isbnQuery.length() > 0) {
        isbnQuery.append(" OR ");
      }
      isbnQuery.append(isbn);
    }
    sb.append("+isbn:(").append(isbnQuery.toString()).append(")");
    return sb.toString();
  }

  private int getNrOfRecords(final String responseDcxXml) throws ConnectorException {
    final XPath xpath = XPathFactory.newInstance().newXPath();
    try {
      InputSource src = new InputSource(new StringReader(responseDcxXml));
      final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      final Document xml = builder.parse(src);

      return Integer.valueOf(xpath.evaluate("/searchRetrieveResponse/numberOfRecords", xml));
    } catch (XPathExpressionException | ParserConfigurationException | SAXException
        | IOException e) {
      throw new ConnectorException("error while parsing reply", e);
    }
  }

  @Override
  public boolean contains(final DocumentMetadata documentMetadata) throws ConnectorException {
    final String request = buildRequest(documentMetadata);
    LOG.debug("retrieveDocument with request: {}", request);
    final ResponseEntity<String> response = restTemplate.getForEntity(request, String.class);
    LOG.debug("response: {}", response.getStatusCode().toString());
    boolean contained = false;
    if (HttpStatus.OK.equals(response.getStatusCode()) && response.hasBody()) {
      LOG.debug("response body: {}", response.getBody());
      int nrOfExistingDocuments = getNrOfRecords(response.getBody());
      contained = nrOfExistingDocuments > 0;
    } else {
      throw new ConnectorException("Cannot retrieve document from: " + baseUrl);
    }
    LOG.debug("contained in inventory: {}", contained);
    return contained;
  }

}
