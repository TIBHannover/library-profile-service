package eu.tib.profileservice.connector;

import eu.tib.profileservice.domain.DocumentMetadata;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DnbConnector implements InstitutionConnector {

  private static final Logger LOG = LoggerFactory.getLogger(DnbConnector.class);

  private static final String SET_NEW_PUBLICATIONS = "dnb-all:reiheN";
  private static final String REPOSITORY = "repository";
  private static final String RESULT_FORMAT = "MARC21-xml";
  private static final String MARCXML_RECORD_PATH = "/OAI-PMH/ListRecords/record/metadata/record";
  private static final String RESUMPTION_TOKEN_PATH = "/OAI-PMH/ListRecords/resumptionToken";

  private final RestTemplate restTemplate;
  private final String baseUrl;
  private final String accessToken;
  private final LocalDate from;
  private final LocalDate to;

  private String resumptionToken;
  private boolean isFirstRequest;

  private boolean errorOccurred;

  /**
   * New instance of {@link DnbConnector}.
   * 
   * @param restTemplate restTemplate
   * @param baseUrl baseUrl
   * @param accessToken accessToken
   * @param from from
   * @param to to
   */
  public DnbConnector(final RestTemplate restTemplate, final String baseUrl,
      final String accessToken, final LocalDate from,
      final LocalDate to) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
    this.accessToken = accessToken;
    this.from = from;
    this.to = to;
    resumptionToken = null;
    isFirstRequest = true;
    errorOccurred = false;
  }

  private String buildRequestRetrieveRecords() {
    final StringBuilder sb = new StringBuilder();
    sb.append(baseUrl);
    sb.append("/accessToken~").append(accessToken);
    sb.append("/").append(REPOSITORY);
    sb.append("?verb=ListRecords");
    if (resumptionToken != null) {
      sb.append("&resumptionToken=").append(resumptionToken);
    } else {
      if (from != null) {
        sb.append("&from=").append(from.format(DateTimeFormatter.ISO_LOCAL_DATE));
      }
      if (to != null) {
        sb.append("&until=").append(to.format(DateTimeFormatter.ISO_LOCAL_DATE));
      }
      sb.append("&set=").append(SET_NEW_PUBLICATIONS);
      sb.append("&metadataPrefix=").append(RESULT_FORMAT);
    }
    return sb.toString();
  }

  @Override
  public List<DocumentMetadata> retrieveNextDocuments() {
    LOG.debug("retrieveDocuments from DNB");
    isFirstRequest = false;
    List<DocumentMetadata> documents = null;
    final String request = buildRequestRetrieveRecords();
    LOG.debug("retrieveDocuments with request: {}", request.replace(accessToken, "xxx"));
    try {
      final ResponseEntity<String> response = restTemplate.getForEntity(request, String.class);
      LOG.debug("response: {}", response.getStatusCode().toString());
      if (HttpStatus.OK.equals(response.getStatusCode()) && response.hasBody()) {
        MarcXml2DocumentConverter converter = new MarcXml2DocumentConverter();
        documents = converter.extractMarcXmlRecordsAndConvert(MARCXML_RECORD_PATH, response
            .getBody());
        if (converter.hasErrors()) {
          LOG.warn("Errors occurred during data conversion");
          errorOccurred = true;
          converter.getErrors().forEach(e -> LOG.warn(e.toString()));
        }
        resumptionToken = getResumptionToken(response.getBody());
        LOG.debug("Got resumption token {}", resumptionToken);
      } else {
        LOG.warn("Cannot retrieve data from DNB ({}) - status: {}", request.replace(accessToken,
            "xxx"), response.getStatusCode());
        errorOccurred = true;
        resumptionToken = null;
      }
    } catch (RestClientException e) {
      LOG.error("Error while accessing: " + baseUrl, e);
      errorOccurred = true;
      resumptionToken = null;
    }
    return documents;
  }

  private String getResumptionToken(final String oaiResponse) {
    try {
      final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      final Document xml = builder.parse(new InputSource(new StringReader(oaiResponse)));
      final XPath xpath = XPathFactory.newInstance().newXPath();
      return (String) xpath.compile(RESUMPTION_TOKEN_PATH).evaluate(xml, XPathConstants.STRING);
    } catch (IOException | ParserConfigurationException | XPathExpressionException
        | SAXException e) {
      LOG.warn("Cannot determine resumption token");
    }
    return null;
  }

  @Override
  public boolean hasNext() {
    return isFirstRequest || resumptionToken != null && resumptionToken.length() > 0;
  }


  @Override
  public boolean hasErrors() {
    return errorOccurred;
  }

}
