package eu.tib.profileservice.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import eu.tib.profileservice.domain.DocumentMetadata;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.Test;

public class MarcXml2DocumentConverterTest {

  @Test
  public void testConvertMarcXmlRecordInvalid() throws IOException {
    MarcXml2DocumentConverter converter = new MarcXml2DocumentConverter();
    converter.convertMarcXmlRecords("invalid xml");
    assertTrue(converter.hasErrors());

    converter = new MarcXml2DocumentConverter();
    converter.convertMarcXmlRecords("<invalidmarcxml></invalidmarcxml>");
    assertTrue(converter.hasErrors());
  }

  @Test
  public void testConvertMarcXmlRecordDnbStyle() throws IOException {
    final MarcXml2DocumentConverter converter = new MarcXml2DocumentConverter();
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(
        "connector/MarcXmlRecord001.xml")) {
      final List<DocumentMetadata> result = converter.convertMarcXmlRecords(is);
      assertNotNull(result);
      assertEquals(1, result.size());
      final DocumentMetadata metadata = result.get(0);
      assertEquals("Test Title Test", metadata.getTitle());
      assertEquals("Test Remainder Title Test", metadata.getRemainderOfTitle());
      assertEquals(2, metadata.getIsbns().size());
      assertTrue(metadata.getIsbns().contains("1234567890"));
      assertTrue(metadata.getIsbns().contains("9876543210987"));
      assertEquals("Verlag XYZ", metadata.getPublisher());
      assertEquals("EUR 34.99 (DE), EUR 35.83 (AT)", metadata.getTermsOfAvailability());
    }
  }

  @Test
  public void testConvertMarcXmlRecordLocStyle() throws IOException {
    // TODO das Beispiel entspricht keiner Neuerscheinung -> besseres Beispiel
    final MarcXml2DocumentConverter converter = new MarcXml2DocumentConverter();
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(
        "connector/MarcXmlRecord002.xml")) {
      final List<DocumentMetadata> result = converter.convertMarcXmlRecords(is);
      assertNotNull(result);
      assertEquals(1, result.size());
      final DocumentMetadata metadata = result.get(0);
      assertEquals("Test Title Test", metadata.getTitle());
      assertEquals(null, metadata.getRemainderOfTitle());
      assertEquals(1, metadata.getIsbns().size());
      assertEquals("1234567890", metadata.getIsbns().get(0));
      assertEquals("Verlag XYZ", metadata.getPublisher());
    }
  }

  @Test
  public void testConvertMarcXmlRecordWithMissingSubfield() throws IOException {
    final MarcXml2DocumentConverter converter = new MarcXml2DocumentConverter();
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(
        "connector/MarcXmlRecord003.xml")) {
      final List<DocumentMetadata> result = converter.convertMarcXmlRecords(is);
      assertNotNull(result);
      assertEquals(1, result.size());
      final DocumentMetadata metadata = result.get(0);
      assertEquals(1, metadata.getIsbns().size());
    }
  }

  @Test
  public void testExtractMarcXmlRecordAndConvert() throws IOException {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(
        "connector/DNBResponse001.xml")) {
      List<DocumentMetadata> result = new MarcXml2DocumentConverter()
          .extractMarcXmlRecordsAndConvert("/OAI-PMH/ListRecords/record/metadata/record", is);
      assertNotNull(result);
      assertEquals(2, result.size());
    }
  }

}
