package eu.tib.profileservice.connector;

import eu.tib.profileservice.domain.DocumentMetadata;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component("DnbConnector")
@PropertySource("classpath:application.properties")
public class DnbConnector implements InstitutionConnector {

  private static final Logger LOG = LoggerFactory.getLogger(DnbConnector.class);

  private static final String SET_NEW_PUBLICATIONS = "dnb-all:reiheN";
  private static final String REPOSITORY = "repository";
  private static final String RESULT_FORMAT = "MARC21-xml";
  private static final String RESPONSE_MARCXML_RECORD_PATH = "/OAI-PMH/ListRecords/record/metadata/record";

  @Autowired
  private RestTemplate restTemplate;

  @Value("${externalsystem.dnb.baseurl}")
  private String baseUrl;
  @Value("${externalsystem.dnb.token}")
  private String accessToken;

  private String buildRequestRetrieveRecords(final LocalDate from, final LocalDate to) {
    final StringBuilder sb = new StringBuilder();
    sb.append(baseUrl);
    sb.append("/accessToken~").append(accessToken);
    sb.append("/").append(REPOSITORY);
    sb.append("?verb=ListRecords");
    if (from != null) {
      sb.append("&from=").append(from.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
    if (to != null) {
      sb.append("&until=").append(to.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
    sb.append("&set=").append(SET_NEW_PUBLICATIONS);
    sb.append("&metadataPrefix=").append(RESULT_FORMAT);
    return sb.toString();
  }

  @Override
  public List<DocumentMetadata> retrieveDocuments(final LocalDate from, final LocalDate to) {
    LOG.debug("retrieveDocuments from DNB");
    final String request = buildRequestRetrieveRecords(from, to);
    LOG.debug("retrieveDocuments with request: {}", request);
    final ResponseEntity<String> response = restTemplate.getForEntity(request, String.class);
    LOG.debug("response: {}", response.getStatusCode().toString());
    List<DocumentMetadata> documents = null;
    if (HttpStatus.OK.equals(response.getStatusCode()) && response.hasBody()) {
      //LOG.debug(response.getBody());
      MarcXml2DocumentConverter converter = new MarcXml2DocumentConverter();
      documents = converter.extractMarcXmlRecordsAndConvert(RESPONSE_MARCXML_RECORD_PATH, response
          .getBody());
      if (converter.hasErrors()) {
        LOG.warn("Errors occurred during data conversion");
        converter.getErrors().forEach(e -> LOG.warn(e.toString()));
      }
    } else {
      LOG.warn("Cannot retrieve data from DNB ({}) - status: {}", request, response
          .getStatusCode());
    }
    return documents;
  }

}
