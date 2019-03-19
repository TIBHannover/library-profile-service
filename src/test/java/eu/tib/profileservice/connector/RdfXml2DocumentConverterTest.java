package eu.tib.profileservice.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import eu.tib.profileservice.domain.DocumentMetadata;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class RdfXml2DocumentConverterTest {

  private RdfXml2DocumentConverter converter;

  @Before
  public void setUp() {
    converter = new RdfXml2DocumentConverter();
  }

  @Test
  public void testConvertRdfXmlRecordInvalid() throws IOException {
    try (ByteArrayInputStream is = new ByteArrayInputStream("invalid xml".getBytes())) {
      converter.convertRdfXmlRecords(is);
      assertTrue(converter.hasErrors());
    }
  }

  @Test
  public void testConvertRdfXmlRecords() throws IOException {
    try (ByteArrayInputStream is = new ByteArrayInputStream("<rdf:RDF><empty/></rdf:RDF>"
        .getBytes())) {
      final List<DocumentMetadata> result = converter.convertRdfXmlRecords(is);
      assertThat(result.size()).isEqualTo(0);
    }

    try (InputStream is = getClass().getClassLoader().getResourceAsStream(
        "connector/RdfXmlRecord001.xml")) {
      final List<DocumentMetadata> result = converter.convertRdfXmlRecords(is);
      assertThat(result).isNotNull();
      assertThat(result.size()).isEqualTo(1);
      final DocumentMetadata metadata = result.get(0);
      assertThat(metadata.getTitle()).isEqualTo("Testtitle");
      assertThat(metadata.getIsbns()).containsExactlyInAnyOrder("9781786724441", "1786724448");
      assertThat(metadata.getPublisher()).isEqualTo("test publisher");
      assertThat(metadata.getPlaceOfPublication()).isEqualTo("testplace");
      assertThat(metadata.getDateOfPublication()).isEqualTo("2006");
      assertThat(metadata.getEdition()).isEqualTo("test edition");
      assertThat(metadata.getPhysicalDescription()).isEqualTo("1 test resource (xi, 260 pages)");
      assertThat(metadata.getSeries()).isEqualTo("series1, series2");
      assertThat(metadata.getTermsOfAvailability()).isEqualTo("£9.50");
      assertThat(metadata.getAuthors()).containsExactlyInAnyOrder("test author1", "test author2",
          "test author3");
      assertThat(metadata.getDeweyDecimalClassifications()).containsExactlyInAnyOrder("950",
          "949.65");
      assertThat(metadata.getFormKeywords()).containsExactlyInAnyOrder("test book", "another test");
    }

    try (InputStream is = getClass().getClassLoader().getResourceAsStream(
        "connector/RdfXmlRecord002.xml")) {
      final List<DocumentMetadata> result = converter.convertRdfXmlRecords(is);
      assertThat(result).isNotNull();
      assertThat(result.size()).isEqualTo(2);
    }
  }

}
