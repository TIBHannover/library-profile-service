package eu.tib.profileservice.connector;

import eu.tib.profileservice.domain.DocumentMetadata;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Converter that converts rdf-xml-document into {@link DocumentMetadata}s.
 */
public class RdfXml2DocumentConverter {

  private static final Logger LOG = LoggerFactory.getLogger(RdfXml2DocumentConverter.class);

  private boolean errorOccurred;

  /**
   * Convert the given rdf xml into {@link DocumentMetadata}s.
   * @param xmlInputStream xml document stream
   * @return documents
   */
  public List<DocumentMetadata> convertRdfXmlRecords(final InputStream xmlInputStream) {
    errorOccurred = false;
    final List<DocumentMetadata> documents = new ArrayList<>();
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document xml = builder.parse(xmlInputStream);
      xml.getDocumentElement().normalize();
      NodeList elements = xml.getFirstChild().getChildNodes();
      for (int i = 0; i < elements.getLength(); i++) {
        Node node = elements.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE
            && "rdf:Description".equals(node.getNodeName())) {
          documents.add(toDocument((Element) node));
        }
      }
    } catch (ParserConfigurationException | SAXException | IOException e) {
      LOG.error("Error while parsing rdfxml");
      errorOccurred = true;
    }
    return documents;
  }

  private DocumentMetadata toDocument(final Element element) {
    final DocumentMetadata document = new DocumentMetadata();
    document.setTitle(getElementTextIfExists(element, "dcterms:title")); // includes all 245 entries
    //document.setRemainderOfTitle(); // included in "dcterms:title"
    document.setIsbns(getIsbns(element));
    document.setPublisher(getPublisher(element));
    document.setPlaceOfPublication(getPublicationPlace(element));
    document.setDateOfPublication(getPublicationDate(element));
    document.setEdition(getElementTextIfExists(element, "isbd:P1008"));
    document.setPhysicalDescription(getElementTextIfExists(element, "dcterms:extent",
        "rdfs:label"));
    document.setSeries(getSeries(element));
    //    document.setFormOfProduct(getFormOfProduct(record)); // not contained in bl rdf
    document.setTermsOfAvailability(getElementTextIfExists(element, "rda:termsOfAvailability"));
    document.setAuthors(getAuthors(element));
    document.setDeweyDecimalClassifications(getDeweyClassifications(element));
    document.setFormKeywords(getKeywords(element));

    return document;
  }

  private List<String> getIsbns(final Element element) {
    Set<String> isbns = new HashSet<String>();
    isbns.addAll(getAllElementTexts(element, "bibo:isbn13"));
    isbns.addAll(getAllElementTexts(element, "bibo:isbn10"));
    for (String identifier : getAllElementTexts(element, "dcterms:identifier")) {
      Pattern pattern = Pattern.compile("urn:isbn:(.+)");
      Matcher matcher = pattern.matcher(identifier);
      if (matcher.matches()) {
        isbns.add(matcher.group(1));
      }
    }
    return new ArrayList<String>(isbns);
  }

  private String getPublisher(final Element element) {
    return getElementTextIfExists(element, "dcterms:publisher", "rdfs:label");
  }

  private String getPublicationPlace(final Element element) {
    return getElementTextIfExists(element, "isbd:P1016", "rdfs:label");
  }

  private String getPublicationDate(final Element element) {
    String date = getElementTextIfExists(element, "dcterms:issued");
    if (date == null) {
      date = getElementTextIfExists(element, "dcterms:created");
    }
    return date;
  }

  private String getSeries(final Element element) {
    StringBuilder series = new StringBuilder();
    for (String seriesStatement : getAllElementTexts(element, "rda:seriesStatement")) {
      if (series.length() > 0) {
        series.append(", ");
      }
      series.append(seriesStatement);
    }
    return series.toString();
  }

  private List<String> getAuthors(final Element element) {
    Set<String> authors = new HashSet<String>();
    List<Element> elements;
    elements = getPersonResourceElements(element, "dcterms:creator");
    authors.addAll(getAllElementTexts(elements, "rdfs:label"));

    elements = getPersonResourceElements(element, "dcterms:contributor");
    authors.addAll(getAllElementTexts(elements, "rdfs:label"));

    return new ArrayList<String>(authors);
  }

  private Set<String> getDeweyClassifications(final Element element) {
    final String type = "http://dewey.info/schema-terms/Notation";
    List<Element> elements = getElements(element, "skos:notation").stream()
        .filter(e -> type.equals(e.getAttribute("rdf:datatype")))
        .collect(Collectors.toList());
    return new HashSet<String>(getAllElementTexts(elements));
  }

  private List<String> getKeywords(final Element element) {
    List<Element> elements = getElements(element, "dcterms:subject");
    elements = filter(elements, "skos:inScheme", "rdf:resource",
        "http://id.loc.gov/authorities/subjects");
    Set<String> unique = new HashSet<String>(getAllElementTexts(elements, "rdfs:label"));
    return new ArrayList<String>(unique);
  }

  private List<Element> getElements(final Element element, final String elementTagName) {
    List<Element> result = new ArrayList<>();
    NodeList elements = element.getElementsByTagName(elementTagName);
    for (int i = 0; i < elements.getLength(); i++) {
      result.add((Element) elements.item(i));
    }
    return result;
  }

  /**
   * Get all elements matching the given tag name with existing person-resource-type.
   * @param element root element
   * @param elementTagName result elements have to match this tag
   * @return elements
   */
  private List<Element> getPersonResourceElements(final Element element,
      final String elementTagName) {
    List<Element> elements = getElements(element, elementTagName);
    return filter(elements, "rdf:type", "rdf:resource", "http://xmlns.com/foaf/0.1/Person");
  }

  /**
   * Get all elements from the given list with a matching tag that has the given matching attribute.
   * @param elements elements to filter
   * @param tagName element has to include a sub-element matching this tag
   * @param attributeName sub-element has to contain this attribute key
   * @param attributeValue sub-element has to contain this attribute value
   * @return
   */
  private List<Element> filter(final List<Element> elements, final String tagName,
      final String attributeName, final String attributeValue) {
    return elements.stream()
        .filter(e -> e.getElementsByTagName(tagName).getLength() > 0
            && attributeValue.equals(
                ((Element) e.getElementsByTagName(tagName).item(0)).getAttribute(attributeName)))
        .collect(Collectors.toList());
  }

  /**
   * Get texts from the elements that matches the given tag.
   * @param element root element
   * @param elementTagName tag the elements have to match
   * @return all texts
   */
  private List<String> getAllElementTexts(final Element element, final String elementTagName) {
    return getAllElementTexts(getElements(element, elementTagName));
  }

  /**
   * Get texts from the elements.
   *
   * @param elements elements to get text from (directly)
   * @return all texts
   */
  private List<String> getAllElementTexts(final List<Element> elements) {
    return getAllElementTexts(elements, null);
  }

  /**
   * Get texts from the elements. May consider sub-elements.
   *
   * @param elements elements to get text from (directly or via sub-element)
   * @param subElementTagName if not null, get text from the sub-elements matching this tag;
   *     otherwise, get text directly from the element;
   * @return all texts
   */
  private List<String> getAllElementTexts(final List<Element> elements,
      final String subElementTagName) {
    List<String> result = new ArrayList<String>();
    for (Element element : elements) {
      if (subElementTagName == null) {
        result.add(element.getTextContent());
      } else {
        String s = getElementTextIfExists(element, subElementTagName);
        if (s != null) {
          result.add(s);
        }
      }
    }
    return result;
  }

  private String getElementTextIfExists(final Element element, final String elementTagName,
      final String subElementTagName) {
    NodeList elements = element.getElementsByTagName(elementTagName);
    if (elements.getLength() > 0) {
      return getElementTextIfExists((Element) elements.item(0), subElementTagName);
    }
    return null;
  }

  private String getElementTextIfExists(final Element element, final String elementTagName) {
    NodeList elementsByTagName = element.getElementsByTagName(elementTagName);
    if (elementsByTagName.getLength() > 0) {
      return elementsByTagName.item(0).getTextContent();
    }
    return null;
  }

  /**
   * Check, if there was an error during conversion.
   *
   * @return true, if there was an error during conversion; false, otherwise
   */
  public boolean hasErrors() {
    return errorOccurred;
  }

}
