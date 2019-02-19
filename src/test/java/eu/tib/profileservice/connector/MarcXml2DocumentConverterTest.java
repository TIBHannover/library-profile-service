package eu.tib.profileservice.connector;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;

import eu.tib.profileservice.domain.DocumentMetadata;

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
	public void testConvertMarcXmlRecord() throws IOException {
		final MarcXml2DocumentConverter converter = new MarcXml2DocumentConverter();
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("connector/MarcXmlRecord001.xml")) {
			final List<DocumentMetadata> result = converter.convertMarcXmlRecords(is);
			assertNotNull(result);
			assertEquals(1, result.size());
			final DocumentMetadata metadata = result.get(0);
			assertEquals("Test Title Test", metadata.getTitle());
			assertEquals("Test Remainder Title Test", metadata.getRemainderOfTitle());
			assertEquals(2, metadata.getIsbns().size());
			assertTrue(metadata.getIsbns().contains("1234567890"));
			assertTrue(metadata.getIsbns().contains("9876543210987"));
		}
	}

	@Test
	public void testExtractMarcXmlRecordAndConvert() throws IOException {
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("connector/DNBResponse001.xml")) {
			List<DocumentMetadata> result = new MarcXml2DocumentConverter()
					.extractMarcXmlRecordsAndConvert("/OAI-PMH/ListRecords/record/metadata/record", is);
			assertNotNull(result);
			assertEquals(2, result.size());
		}
	}

}
