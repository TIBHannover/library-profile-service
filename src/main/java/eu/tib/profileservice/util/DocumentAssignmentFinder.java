package eu.tib.profileservice.util;

import eu.tib.profileservice.domain.Category;
import eu.tib.profileservice.domain.DocumentMetadata;
import eu.tib.profileservice.domain.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Helper class that determines the user a document should be assigned to.
 * <p>
 * Considers Dewey-Classification of the given documents and DDC-categories assigned to the users.
 * </p>
 */
public class DocumentAssignmentFinder {

  private final Map<Category, User> usersByDdcCategory;
  private final List<Category> ddcCategories;

  /**
   * Constructor of {@link DocumentAssignmentFinder}.
   * 
   * @param users all users who may become assignee
   */
  public DocumentAssignmentFinder(List<User> users) {
    this.usersByDdcCategory = new HashMap<Category, User>();
    for (User user : users) {
      List<Category> categories = user.getCategories();
      if (categories != null) {
        for (Category category : categories) {
          if (Category.Type.DDC.equals(category.getType())) {
            usersByDdcCategory.put(category, user);
          }
        }
      }
    }

    this.ddcCategories = new ArrayList<>(usersByDdcCategory.keySet());
    // sort by length of the dewey-classification => longer classification has priority
    Collections.sort(this.ddcCategories, new Comparator<Category>() {
      @Override
      public int compare(Category cat0, Category cat1) {
        String arg0 = cat0.getCategory();
        String arg1 = cat1.getCategory();
        int result = 0;
        result = arg1.length() - arg0.length();
        if (result == 0) {
          result = arg1.compareTo(arg0);
        }
        return result;
      }
    });
  }

  /**
   * Determine the {@link User} the given {@link DocumentMetadata} should be assigned to.
   * 
   * @param documentMetadata document
   * @return the assignee; null, if no assignee could be determined
   */
  public User determineAssignee(final DocumentMetadata documentMetadata) {
    if (documentMetadata.getDeweyDecimalClassifications() != null && documentMetadata
        .getDeweyDecimalClassifications().size() > 0) {
      return determineAssigneeByDeweyClassification(documentMetadata);
    }
    return null;
  }

  /**
   * Determine the {@link User} the given {@link DocumentMetadata} should be assigned to. Use
   * Dewey-Classification to determine the user.
   * 
   * @param documentMetadata document
   * @return the assignee; null, if no assignee could be determined
   */
  private User determineAssigneeByDeweyClassification(final DocumentMetadata documentMetadata) {
    // sort by length of the dewey-classification => longer classification has priority
    SortedSet<String> ddcs = new TreeSet<String>(new Comparator<String>() {
      @Override
      public int compare(String arg0, String arg1) {
        int result = 0;
        result = arg1.length() - arg0.length();
        if (result == 0) {
          result = arg1.compareTo(arg0);
        }
        return result;
      }
    });
    ddcs.addAll(documentMetadata.getDeweyDecimalClassifications());

    int accuracy = 0;
    Category bestMatchingCategory = null;
    for (String ddc : ddcs) {
      Category matchingCategory = getMatchingCategory(ddc);
      if (matchingCategory != null) {
        int curAccuracy = getAccuracy(ddc, matchingCategory.getCategory());
        if (bestMatchingCategory == null || curAccuracy > accuracy) {
          accuracy = curAccuracy;
          bestMatchingCategory = matchingCategory;
        }
      }
    }
    if (bestMatchingCategory != null) {
      return usersByDdcCategory.get(bestMatchingCategory);
    }
    return null;
  }

  private Category getMatchingCategory(final String ddc) {
    for (Category category : ddcCategories) {
      if (categoryIncludesDdc(category, ddc)) {
        return category;
      }
    }
    return null;
  }

  /**
   * Check if the given ddc-String is included in the given ddc-Category. Any trailing '0' of the
   * given {@link Category} may be truncated.
   * 
   * @param ddcCategory ddc category
   * @param ddc ddc
   * @return
   */
  private boolean categoryIncludesDdc(final Category ddcCategory, final String ddc) {
    String category = ddcCategory.getCategory();
    // remove trailing 0s
    while (category.length() > 1 && category.charAt(category.length() - 1) == '0') {
      category = category.substring(0, category.length() - 1);
    }
    String adjustedDdc = ddc;
    if (adjustedDdc.length() > category.length()) {
      adjustedDdc = adjustedDdc.substring(0, category.length());
    }
    return category.equals(adjustedDdc);
  }

  /**
   * Get Accuracy of both classifications; this means: count matching chars at the beginning.
   * 
   * @param s1 dewey decimal classification 1
   * @param s2 dewey decimal classification 2
   * @return
   */
  private int getAccuracy(final String s1, final String s2) {
    int accuracy = 0;
    for (; accuracy < Math.min(s1.length(), s2.length()); accuracy++) {
      if (s1.charAt(accuracy) != s2.charAt(accuracy)) {
        break;
      }
    }
    return accuracy;
  }

}
