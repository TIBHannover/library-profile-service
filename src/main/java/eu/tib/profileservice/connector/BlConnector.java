package eu.tib.profileservice.connector;

import eu.tib.profileservice.domain.DocumentMetadata;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
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
  private final List<String> rdfFileNames;
  private Integer rdfFileIdx;
  private boolean errorOccurred;

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
    this.errorOccurred = false;
    rdfFileIdx = null;
    rdfFileNames = determineRdfFilesForDateRange(from, to);
  }

  private String getBndRdfFilename(final long fileNumber) {
    final StringBuilder sb = new StringBuilder();
    sb.append("bnbrdf_N").append(fileNumber).append(".zip");
    return sb.toString();
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
    // TODO: change approach
    // approach: determine file number from start date / start file number.
    // => have to update skippedDates each time a date is skipped...
    // first date 2019: 2019-01-02, Number 3526
    final long startFileNumber = 3525;
    final LocalDate startDate = LocalDate.parse("2018-12-19");
    final String[] skippedDates = new String[] {"2018-12-26"};
    long fromFileNumber = startFileNumber + ChronoUnit.WEEKS.between(startDate, from)
        - skippedDates.length;
    long toFileNumber = startFileNumber + ChronoUnit.WEEKS.between(startDate, to)
        - skippedDates.length;
    return LongStream.rangeClosed(fromFileNumber, toFileNumber)
        .mapToObj(l -> getBndRdfFilename(l))
        .collect(Collectors.toList());
  }

  @Override
  public boolean hasNext() {
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

    rdfFileIdx = rdfFileIdx == null ? 0 : (rdfFileIdx + 1);
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
