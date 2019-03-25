package eu.tib.profileservice.connector;

import eu.tib.profileservice.domain.DocumentMetadata;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Connector to retrieve data from the British Library.
 */
public class BlConnector implements InstitutionConnector {

  private static final Logger LOG = LoggerFactory.getLogger(BlConnector.class);

  private final RestTemplate restTemplate;
  private final String baseUrl;
  private final LocalDate from;
  private final LocalDate to;
  private List<String> rdfFileNames;
  private Integer rdfFileIdx;
  private boolean errorOccurred;
  private boolean initialized;

  /**
   * Constructor of {@link BlConnector}.
   * 
   * @param restTemplate restTemplate
   * @param baseUrl baseUrl
   * @param from from
   * @param to to
   */
  public BlConnector(final RestTemplate restTemplate, final String baseUrl, final LocalDate from,
      final LocalDate to) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
    this.from = from;
    this.to = to;
    this.errorOccurred = false;
    rdfFileIdx = null;
    initialized = false;
  }

  /**
   * Initialize considered rdf files for the date range.
   */
  protected void initialize() {
    rdfFileNames = determineRdfFilesForDateRange(from, to);
    initialized = true;
  }

  /**
   * Determine filenames for the given date range.
   * 
   * @param from from date
   * @param to to date
   * @return filenames
   */
  protected List<String> determineRdfFilesForDateRange(final LocalDate from, final LocalDate to) {
    if (from.isAfter(to)) {
      return new ArrayList<String>();
    }

    // approach: determine available files from html-overview-page from bl.uk
    // => cannot find a better solution :(
    List<String> resultFiles = new ArrayList<String>();
    try {
      Map<LocalDate, String> availableFiles = new HashMap<LocalDate, String>();
      final String request = "http://www.bl.uk/bibliographic/bnbrdfxml.html";
      ResponseEntity<String> response = restTemplate.getForEntity(request, String.class);
      if (HttpStatus.OK.equals(response.getStatusCode()) && response.hasBody()) {
        String rdfOverviewHtml = response.getBody();
        for (String line : rdfOverviewHtml.split(System.getProperty("line.separator"))) {
          Pattern pattern = Pattern.compile(
              "bnbrdf/(bnbrdf_N[0-9]{4,}.zip)\\\">([0-3][0-9]/[01][0-9]/[0-9]{4})");
          Matcher matcher = pattern.matcher(line);
          if (matcher.find()) {
            String fileName = matcher.group(1);
            LocalDate date = LocalDate.parse(matcher.group(2), DateTimeFormatter.ofPattern(
                "dd/MM/yyyy"));
            availableFiles.put(date, fileName);
          }
        }
        LOG.debug("available files: {}", availableFiles);
        for (LocalDate date : availableFiles.keySet()) {
          if (date.compareTo(from) >= 0 && date.compareTo(to) <= 0) {
            resultFiles.add(availableFiles.get(date));
          }
        }
        Collections.sort(resultFiles);
        LOG.debug("considered files: {}", resultFiles);
      } else {
        LOG.warn("Cannot retrieve info from BL ({}) - status: {}", request, response
            .getStatusCode());
        errorOccurred = true;
      }
    } catch (RestClientException e) {
      errorOccurred = true;
      LOG.error("error while retrieving rdf info from bnb", e);
    }
    return resultFiles;

    //    // approach: determine file number from start date / start file number.
    //    // => have to update skippedDates each time a date is skipped :(
    //    // first date 2019: 2019-01-02, Number 3526
    //    final long startFileNumber = 3525;
    //    final LocalDate startDate = LocalDate.parse("2018-12-19");
    //    final String[] skippedDates = new String[] {"2018-12-26"};
    //    long fromFileNumber = startFileNumber + ChronoUnit.WEEKS.between(startDate, from)
    //        - skippedDates.length;
    //    long toFileNumber = startFileNumber + ChronoUnit.WEEKS.between(startDate, to)
    //        - skippedDates.length;
    //    return LongStream.rangeClosed(fromFileNumber, toFileNumber)
    //        .mapToObj(l -> getBndRdfFilename(l))
    //        .collect(Collectors.toList());
    //    private String getBndRdfFilename(final long fileNumber) {
    //      final StringBuilder sb = new StringBuilder();
    //      sb.append("bnbrdf_N").append(fileNumber).append(".zip");
    //      return sb.toString();
    //    }
    //
  }

  @Override
  public boolean hasNext() {
    if (!initialized) {
      initialize();
    }
    if (errorOccurred || rdfFileNames.isEmpty()) {
      return false;
    }
    return rdfFileIdx == null || rdfFileIdx + 1 < rdfFileNames.size();
  }

  private String buildRequest(final String rdfFile) {
    final StringBuilder sb = new StringBuilder();
    sb.append(baseUrl).append("/").append(rdfFile);
    return sb.toString();
  }

  @Override
  public List<DocumentMetadata> retrieveNextDocuments() {
    LOG.debug("retrieveDocuments from BL");
    if (!initialized) {
      initialize();
    }

    rdfFileIdx = rdfFileIdx == null ? 0 : rdfFileIdx + 1;
    String rdfFile = rdfFileNames.get(rdfFileIdx);
    String request = buildRequest(rdfFile);
    LOG.debug("retrieveDocuments with request: {}", request);
    List<DocumentMetadata> documents = new ArrayList<>();
    try {
      ResponseEntity<byte[]> response = restTemplate.getForEntity(request, byte[].class);
      LOG.debug("response: {}", response.getStatusCode().toString());
      if (HttpStatus.OK.equals(response.getStatusCode()) && response.hasBody()) {

        // rdf zip contains just one entry; multiple entries are not possible wit ZipInputStream,
        // because the domdocumentbuilder closes the stream
        // -> multiple entries require ZipFile instead of stream or wrapper
        try (ByteArrayInputStream bis = new ByteArrayInputStream(response.getBody());
            ZipInputStream zipStream = new ZipInputStream(bis)) {
          ZipEntry entry = zipStream.getNextEntry();
          if (entry == null) {
            LOG.error("Cannot get content from zip file");
            errorOccurred = true;
          } else {
            LOG.debug("process zip entry: {}", entry.getName());
            RdfXml2DocumentConverter converter = new RdfXml2DocumentConverter();
            documents.addAll(converter.convertRdfXmlRecords(zipStream));
            if (converter.hasErrors()) {
              errorOccurred = true;
            }
          }
        } catch (IOException e) {
          LOG.error("error while parsing rdf-zip-file", e);
          errorOccurred = true;
        }
      } else {
        LOG.warn("Cannot retrieve data from BL ({}) - status: {}", request, response
            .getStatusCode());
        errorOccurred = true;
      }
    } catch (RestClientException e) {
      LOG.error("Error while accessing: " + baseUrl, e);
      errorOccurred = true;
    }

    return documents;
  }

  @Override
  public boolean hasErrors() {
    return errorOccurred;
  }

}
