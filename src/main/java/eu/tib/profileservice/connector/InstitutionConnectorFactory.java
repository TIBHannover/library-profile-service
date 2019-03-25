package eu.tib.profileservice.connector;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component()
@PropertySource("classpath:application.properties")
public class InstitutionConnectorFactory {

  /** type of the institution connector. */
  public enum ConnectorType {
    /** type for british library. */
    BL,
    /** deutsche nationalbibliothek. */
    DNB
  }

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private Environment env;

  /**
   * Create a new {@link InstitutionConnector} for the given date range and of the given
   * {@link ConnectorType}.
   *
   * @param connectorType type
   * @param from from date
   * @param to to date
   * @return
   */
  public InstitutionConnector createConnector(final ConnectorType connectorType,
      final LocalDate from, final LocalDate to) {
    switch (connectorType) {
      case DNB:
        return createDnbConnector(from, to);
      case BL:
        return createBlConnector(from, to);
      default:
        throw new IllegalArgumentException("Cannot create connector of type " + connectorType);
    }
  }

  private InstitutionConnector createDnbConnector(final LocalDate from, final LocalDate to) {
    String baseUrl = env.getProperty("externalsystem.dnb.baseurl");
    String accessToken = env.getProperty("externalsystem.dnb.token");
    return new DnbConnector(restTemplate, baseUrl, accessToken, from, to);
  }

  private InstitutionConnector createBlConnector(final LocalDate from, final LocalDate to) {
    String baseUrl = env.getProperty("externalsystem.bl.baseurl");
    return new BlConnector(restTemplate, baseUrl, from, to);
  }
}
