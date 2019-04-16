package eu.tib.profileservice.repository;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.Document.Status;
import eu.tib.profileservice.domain.User;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.jpa.domain.Specification;

/**
 * Prepares the {@link Specification} search for {@link Document}.
 */
public class DocumentSpecification {

  /**
   * {@link Specification} for attribute assignee.
   * @param assignee assignee
   * @return specification
   */
  public static Specification<Document> byAssignee(final User assignee) {
    return (root, query, criteriaBuilder) -> {
      if (assignee == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get(Document.ATTRIBUTE_NAME_ASSIGNEE), assignee);
    };
  }

  /**
   * {@link Specification} for attribute status.
   * @param status status
   * @return specification
   */
  public static Specification<Document> byStatus(final Status status) {
    return (root, query, criteriaBuilder) -> {
      if (status == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get(Document.ATTRIBUTE_NAME_STATUS), status);
    };
  }

  /**
   * {@link Specification} for attribute creationDateUtc.
   * Specification for all documents that were created after (or at) the given from date.
   * @param creationDateFrom from date for creationDate
   * @return specification
   */
  public static Specification<Document> byCreationDateAfterOrEqual(
      final LocalDate creationDateFrom) {
    return (root, query, criteriaBuilder) -> {
      if (creationDateFrom == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.greaterThanOrEqualTo(root.get(Document.ATTRIBUTE_NAME_CREATION_DATE),
          creationDateFrom.atStartOfDay());
    };
  }

  /**
   * {@link Specification} for attribute creationDateUtc.
   * Specification for all documents that were created before (or at) the given to date.
   * @param creationDateTo to date for creationDate
   * @return specification
   */
  public static Specification<Document> byCreationDateBeforeOrEqual(
      final LocalDate creationDateTo) {
    return (root, query, criteriaBuilder) -> {
      if (creationDateTo == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.lessThanOrEqualTo(root.get(Document.ATTRIBUTE_NAME_CREATION_DATE),
          creationDateTo.atTime(LocalTime.MAX));
    };
  }

}
