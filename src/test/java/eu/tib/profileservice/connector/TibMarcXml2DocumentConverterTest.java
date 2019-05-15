package eu.tib.profileservice.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import eu.tib.profileservice.domain.DocumentMetadata;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.Test;

public class TibMarcXml2DocumentConverterTest {

  @Test
  public void testConvertMarcXmlRecord() throws IOException {
    final MarcXml2DocumentConverter converter = new TibMarcXml2DocumentConverter();
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(
        "connector/TibMarcXmlRecord001.xml")) {
      final List<DocumentMetadata> result = converter.convertMarcXmlRecords(is);
      assertNotNull(result);
      assertEquals(1, result.size());
      final DocumentMetadata metadata = result.get(0);

      assertNotNull(metadata.getInventoryUris());
      assertThat(metadata.getInventoryUris()).containsEntry(
          "https://www.tib.eu/de/suchen/id/TIBKAT%3A011078464", "Portal");
      assertThat(metadata.getInventoryUris()).containsEntry(
          "https://opac.tib.eu/DB=1/LNG=DU/XMLPRS=N/PPN?PPN=011078464", "77-2916");
    }
  }

  @Test
  public void testConvertMarcXmlRecordWithoutInventoryFields() throws IOException {
    final MarcXml2DocumentConverter converter = new TibMarcXml2DocumentConverter();
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(
        "connector/TibMarcXmlRecord002.xml")) {
      final List<DocumentMetadata> result = converter.convertMarcXmlRecords(is);
      assertNotNull(result);
      assertEquals(1, result.size());
      final DocumentMetadata metadata = result.get(0);
      assertThat(metadata.getInventoryUris()).isNullOrEmpty();
    }
  }
}
