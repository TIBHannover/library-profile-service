package eu.tib.profileservice.connector;

import eu.tib.profileservice.domain.DocumentMetadata;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Implementation of {@link InventoryConnector} for sru api of <i>tib.eu</i>.
 *
 * <p>
 * The connector just checks if the numberOfRecords in the response is gt 0.
 * </p>
 */
@PropertySource(value = "file:${envConfigDir:envConf/default/}profileservice.properties")
public class TibConnector implements InventoryConnector {

  private static final Logger LOG = LoggerFactory.getLogger(TibConnector.class);

  //  private static final String RESULT_FORMAT = "dcx";

  private static final String RESULT_FORMAT = "marcxml";
  private static final String MARCXML_RECORD_PATH =
      "/searchRetrieveResponse/records/record/recordData/record";

  @Value("${inventory.tib.baseurl}")
  private String baseUrl;
  @Value("${inventory.tib.recordnrpath}")
  private String pathNumberOfRecords;

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

  //  private int getNrOfRecords(final String responseDcxXml) throws ConnectorException {
  //    final XPath xpath = XPathFactory.newInstance().newXPath();
  //    try {
  //      final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
  //      final Document xml = builder.parse(new InputSource(new StringReader(responseDcxXml)));
  //      return Integer.parseInt(xpath.evaluate(pathNumberOfRecords, xml));
  //    } catch (XPathExpressionException | ParserConfigurationException | SAXException
  //        | IOException e) {
  //      throw new ConnectorException("error while parsing reply", e);
  //    }
  //  }

  @Override
  public boolean processInventoryCheck(final DocumentMetadata documentMetadata)
      throws ConnectorException {
    final String request = buildRequest(documentMetadata);
    LOG.debug("retrieveDocument with request: {}", request);
    try {
      final ResponseEntity<String> response = restTemplate.getForEntity(request, String.class);
      LOG.debug("response: {}", response.getStatusCode().toString());
      boolean contained = false;
      if (HttpStatus.OK.equals(response.getStatusCode()) && response.hasBody()) {
        LOG.debug("response body: {}", response.getBody());
        MarcXml2DocumentConverter converter = new TibMarcXml2DocumentConverter();
        List<DocumentMetadata> documents = converter.extractMarcXmlRecordsAndConvert(
            MARCXML_RECORD_PATH, response.getBody());
        if (converter.hasErrors()) {
          throw new ConnectorException("error while parsing reply");
        }
        if (documents != null && documents.size() > 0) {
          contained = true;
          DocumentMetadata existing = documents.get(0);
          documentMetadata.setInventoryAccessionNumber(existing.getInventoryAccessionNumber());
          documentMetadata.setInventoryUri(existing.getInventoryUri());
        }
      } else {
        throw new ConnectorException("Cannot retrieve document from: " + baseUrl);
      }
      LOG.debug("contained in inventory: {}", contained);
      return contained;
    } catch (RestClientException e) {
      throw new ConnectorException("Error while accessing: " + baseUrl, e);
    }
  }

}
