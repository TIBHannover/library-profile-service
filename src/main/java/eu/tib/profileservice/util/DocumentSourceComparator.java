package eu.tib.profileservice.util;

import eu.tib.profileservice.domain.DocumentMetadata;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Comparator} that compares the source of {@link DocumentMetadata}s.
 */
public class DocumentSourceComparator implements Comparator<DocumentMetadata>, Serializable {

  private static final long serialVersionUID = 1L;

  private Map<String, Integer> priorityBySources;

  /**
   * Constructor of {@link DocumentSourceComparator}.
   * @param sourcePriorityList source list as string, separated by comma.
   *     First source has highest priority.
   */
  public DocumentSourceComparator(final String sourcePriorityList) {
    priorityBySources = new HashMap<String, Integer>();
    String[] sources = sourcePriorityList.split(",");
    for (int i = 0; i < sources.length; i++) {
      priorityBySources.put(sources[i], i + 1);
    }
  }

  @Override
  public int compare(final DocumentMetadata arg0, final DocumentMetadata arg1) {
    Integer prio1 = priorityBySources.get(arg0.getSource());
    Integer prio2 = priorityBySources.get(arg1.getSource());
    if (prio1 == null && prio2 == null) {
      return 0;
    } else if (prio1 == null) {
      return -1;
    } else if (prio2 == null) {
      return 1;
    }
    return prio2 - prio1;
  }

}
