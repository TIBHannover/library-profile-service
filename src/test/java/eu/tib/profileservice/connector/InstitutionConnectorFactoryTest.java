package eu.tib.profileservice.connector;

import static org.junit.Assert.assertTrue;

import eu.tib.profileservice.connector.InstitutionConnectorFactory.ConnectorType;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = InstitutionConnectorFactory.class)
@TestPropertySource(value = "classpath:application.properties")
public class InstitutionConnectorFactoryTest {

  @TestConfiguration
  static class TestContextConfiguration {

    @Bean
    public InstitutionConnectorFactory factory() {
      return new InstitutionConnectorFactory();
    }
  }

  @MockBean
  private RestTemplate restTemplateMock;

  @Autowired
  private InstitutionConnectorFactory factory;

  @Test
  public void testCreateConnector() {
    OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
    LocalDate now = utc.toLocalDate();
    InstitutionConnector connector = factory.createConnector(ConnectorType.DNB, now, now);
    assertTrue(connector instanceof DnbConnector);
  }

}
