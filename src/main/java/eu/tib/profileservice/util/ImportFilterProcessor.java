package eu.tib.profileservice.util;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.Document.Status;
import eu.tib.profileservice.domain.DocumentMetadata;
import eu.tib.profileservice.domain.ImportFilter;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class, that checks, if one of the {@link ImportFilter}s matches on a {@link Document} and
 * processes the appropriate action (for example: set the document-status to
 * {@link Status#IGNORED}).
 */
public class ImportFilterProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(ImportFilterProcessor.class);

  private final List<ImportFilter> filterRules;

  /**
   * Constructor of {@link ImportFilterProcessor}.
   *
   * @param filterRules filter to use
   */
  public ImportFilterProcessor(final List<ImportFilter> filterRules) {
    this.filterRules = filterRules;
  }

  /**
   * Process the {@link ImportFilter}s on the given {@link Document}.
   * <p>
   * If there is a {@link ImportFilter} matching the given document, then the action of the filter
   * will be processed (for example: set the document-status to {@link Status#IGNORED}).
   * </p>
   *
   * @param document document to process
   */
  public void process(final Document document) {
    for (ImportFilter filter : filterRules) {
      if (matches(filter, document.getMetadata())) {
        if (LOG.isDebugEnabled()) {
          final StringBuilder sb = new StringBuilder();
          sb.append("filter ");
          sb.append(filter.getConditionType()).append(": ").append(filter.getCondition());
          sb.append(" matches on ");
          sb.append(document.getMetadata().getTitle());
          sb.append(", ").append(document.getMetadata().getIsbns());
          LOG.debug(sb.toString());
        }
        switch (filter.getAction()) {
          case IGNORE:
            document.setStatus(Status.IGNORED);
            break;
          default:
            break;
        }
        break; // exit on first matching filter
      }
    }
  }

  /**
   * Test if the given filter matches the given document.
   *
   * @param filter filter
   * @param metadata document
   * @return true, if matches; false, otherwise
   */
  private boolean matches(final ImportFilter filter, final DocumentMetadata metadata) {
    if (filter.getCondition() == null || filter.getConditionType() == null) {
      return true; // rules matches always
    }
    boolean matches = false;
    switch (filter.getConditionType()) {
      case CATEGORY:
        Set<String> categrories = metadata.getDeweyDecimalClassifications();
        if (categrories != null) {
          matches = categrories.stream().anyMatch(d -> d.matches(filter.getCondition()));
        }
        break;
      case FORM_KEYWORD:
        List<String> keywords = metadata.getFormKeywords();
        if (keywords != null) {
          matches = keywords.stream().anyMatch(d -> d.matches(filter.getCondition()));
        }
        break;
      default:
        break;
    }
    return matches;
  }

}
