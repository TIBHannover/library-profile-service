package eu.tib.profileservice.connector;

import eu.tib.profileservice.domain.DocumentMetadata;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converter basics for classes that convert records into {@link DocumentMetadata}.
 */
public abstract class Converter {

  /**
   * Remove attached text from isbns.
   * @param rawIsbns isbns that may include text
   * @return isbn without trailing text
   */
  protected List<String> cleanupIsbns(final Collection<String> rawIsbns) {
    Set<String> isbns = new HashSet<>();
    Pattern pattern = Pattern.compile("([0-9X]+).*");
    for (String isbn : rawIsbns) {
      Matcher matcher = pattern.matcher(isbn.replaceAll("-", ""));
      if (matcher.matches()) {
        isbns.add(matcher.group(1));
      } else {
        isbns.add(isbn);
      }
    }
    return new ArrayList<String>(isbns);
  }

}
