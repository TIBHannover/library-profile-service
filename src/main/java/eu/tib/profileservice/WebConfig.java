package eu.tib.profileservice;

import eu.tib.profileservice.connector.InventoryConnector;
import eu.tib.profileservice.connector.TibConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;

@Configuration
@PropertySource(value = "file:${envConfigDir:envConf/default/}profileservice.properties")
public class WebConfig {

  private static final Logger LOG = LoggerFactory.getLogger(WebConfig.class);

  private static final String INVENTORY_CONNECTOR_TIB = "TIB";

  @Value("${inventory.system}")
  private String inventorySystem;

  /**
   * Register the {@link InventoryConnector}-bean, if configured. If not configured, no
   * {@link InventoryConnector} will be used.
   *
   * @return inventoryConnector
   */
  @Bean
  public InventoryConnector inventoryConnector() {
    InventoryConnector connector = null;
    if (inventorySystem == null || inventorySystem.length() == 0) {
      LOG.info("no remote inventory configured");
    } else {
      if (INVENTORY_CONNECTOR_TIB.equalsIgnoreCase(inventorySystem)) {
        connector = new TibConnector();
      } else {
        throw new IllegalArgumentException("illegal inventory system: " + inventorySystem);
      }
      LOG.info("inventory connector {} configured", inventorySystem);
    }
    return connector;
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

}
