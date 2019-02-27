package eu.tib.profileservice.connector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.marc4j.MarcException;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.tib.profileservice.domain.DocumentMetadata;

public class MarcXml2DocumentConverter {

	private static final Logger LOG = LoggerFactory.getLogger(MarcXml2DocumentConverter.class);
	
	public enum ConversionError {
		XPATH_EVALUATION_FAILED,
		NODE_TRANSFORMATION_FAILED,
		IO_EXCEPTION,
		MARCXML_PARSING_FAILED;
	}
	
	private List<ConversionError> errors;

	public MarcXml2DocumentConverter() {
		this.errors = new ArrayList<>();
	}

	/**
	 * Extract all marcxml records from the xml-document with the given xpathExpression and convert the records into {@link DocumentMetadata}s.
	 * <p>
	 * Errors that occur during conversion will be collected
	 * </p>
	 * 
	 * @param xpathExpression the xpathExpression of the marcxml-records
	 * @param xmlInput xml-document as {@link String}
	 * @return converted records; null, if the given xml-document cannot be parsed
	 */
	public List<DocumentMetadata> extractMarcXmlRecordsAndConvert(final String xpathExpression, final String xmlInput) {
		try (ByteArrayInputStream xmlInputStream = new ByteArrayInputStream(xmlInput.getBytes())) {
			return extractMarcXmlRecordsAndConvert(xpathExpression, xmlInputStream);
		} catch (IOException e) {
			LOG.warn("Error closing ByteArrayInputStream", e);
			addConversionError(ConversionError.IO_EXCEPTION);
		}
		return null;
	}
	
	/**
	 * Extract all marcxml records from the xml-document with the given xpathExpression and convert the records into {@link DocumentMetadata}s.
	 * <p>
	 * Errors that occur during conversion will be collected
	 * </p>
	 * 
	 * @param xpathExpression the xpathExpression of the marcxml-records
	 * @param xmlInputStream xml-document as {@link InputStream}
	 * @return converted records; null, if the given xml-document cannot be parsed
	 */
	public List<DocumentMetadata> extractMarcXmlRecordsAndConvert(final String xpathExpression, final InputStream xmlInputStream) {
		final NodeList nodes;
		try {
			final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			final Document xml = builder.parse(xmlInputStream);
			final XPath xpath = XPathFactory.newInstance().newXPath();
			nodes = (NodeList) xpath.compile(xpathExpression).evaluate(xml, XPathConstants.NODESET);
		} catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
			LOG.error("Cannot evaluate xml document with xpathExpression: " + xpathExpression, e);
			addConversionError(ConversionError.XPATH_EVALUATION_FAILED);
			return null;
		}

		final List<DocumentMetadata> documents = new ArrayList<>();
		for (int i = 0; i < nodes.getLength(); i++) {
			final Node node = nodes.item(i);
			try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
				final Transformer xform = TransformerFactory.newInstance().newTransformer();
				xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				xform.transform(new DOMSource(node), new StreamResult(outputStream));
				try (final InputStream recordInputStream = new ByteArrayInputStream(outputStream.toByteArray())) {
					documents.addAll(convertMarcXmlRecords(recordInputStream));
				}
			} catch (IOException | TransformerException e) {
				LOG.error("Error while transforming node", e);
				addConversionError(ConversionError.NODE_TRANSFORMATION_FAILED);
			}
		}
		return documents;
	}

	public List<DocumentMetadata> convertMarcXmlRecords(final String records) {
		try (ByteArrayInputStream recordInputStream = new ByteArrayInputStream(records.getBytes())) {
			return convertMarcXmlRecords(recordInputStream);
		} catch (IOException e) {
			LOG.warn("Error closing ByteArrayInputStream", e);
			addConversionError(ConversionError.IO_EXCEPTION);
		}
		return null;
	}

	public List<DocumentMetadata> convertMarcXmlRecords(final InputStream recordsInputStream) {
		final List<DocumentMetadata> documents = new ArrayList<>();
		try {
			final MarcXmlReader reader = new MarcXmlReader(recordsInputStream);
			while (reader.hasNext()) {
				final Record record = reader.next();
				documents.add(record2Document(record));
			}
		} catch (MarcException e) {
			LOG.warn("Error while parsing marcxml", e);
			addConversionError(ConversionError.MARCXML_PARSING_FAILED);
		}
		return documents;
	}

	private DocumentMetadata record2Document(final Record record) {
		// TODO
		//LOG.debug(record.toString());
		final DocumentMetadata document = new DocumentMetadata();
		
		document.setTitle(getDataIfExists(record, "245", 'a'));		
		document.setRemainderOfTitle(getDataIfExists(record, "245", 'b'));
		document.setIsbns(getAllData(record, "020", 'a'));
		
		final Set<String> deweyDecimalClassifications = new HashSet<>();
		for (VariableField field : record.getVariableFields("082")) {
			if (field instanceof DataField) {
				List<String> classificationNumbers = ((DataField) field).getSubfields('a').stream()
						.map(s -> s.getData()).collect(Collectors.toList());
				deweyDecimalClassifications.addAll(classificationNumbers);

//				String edition = null;
//				Subfield editionSubfield = ((DataField) field).getSubfield('2');
//				if (editionSubfield != null) {
//					edition = editionSubfield.getData();
//				} else {
//					edition = EDITION_NOT_AVAILABLE;
//				}
			}
		}
		document.setDeweyDecimalClassifications(deweyDecimalClassifications);
		
//		document.setAuthor(record.);
//		document.setCategories(categories);
//		document.setDescription(description);
		
		//LOG.debug("Converted document: " + document.getTitle() + ", " + document.getRemainderOfTitle() + ", " + document.getIsbns() + ", " + document.getAuthor() + ", " + document.getCategories() + ", " + document.getDescription());
		return document;
	}
	
	private List<String> getAllData(final Record record, final String tag, char code) {
		final List<VariableField> fields = record.getVariableFields(tag);
		return fields.stream()
			.filter(f -> f instanceof DataField && ((DataField) f).getSubfield(code) != null)
			.map(f -> ((DataField) f).getSubfield(code).getData())
			.collect(Collectors.toList());
	}
	
	private String getDataIfExists(final Record record, final String tag, char code) {
		final DataField field = (DataField) record.getVariableField(tag);
		if (field != null) {
			final Subfield subfield = field.getSubfield(code);
			if (subfield != null) {
				return subfield.getData() == null ? null : subfield.getData().trim();
			}
		}
		return null;
	}
	
	private void addConversionError(final ConversionError conversionError) {
		this.errors.add(conversionError);
	}

	/**
	 * @return the errors that occurred during conversion
	 */
	public List<ConversionError> getErrors() {
		return errors;
	}
	/**
	 * @return true, if there was an error during conversion; false, otherwise
	 */
	public boolean hasErrors() {
		return errors.size() > 0;
	}

}
