package eu.tib.profileservice.domain;

import eu.tib.profileservice.domain.Document.Status;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Search for {@link Document}s.
 */
public class DocumentSearch {

  private Status status;
  private User assignee;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate creationDateFrom;
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate creationDateTo;

  /** getter: status.
   * @return the status
   */
  public Status getStatus() {
    return status;
  }

  /** setter: status.
   * @param status the status to set
   */
  public void setStatus(final Status status) {
    this.status = status;
  }

  /** getter: assignee.
   * @return the assignee
   */
  public User getAssignee() {
    return assignee;
  }

  /** setter: assignee.
   * @param assignee the assignee to set
   */
  public void setAssignee(final User assignee) {
    this.assignee = assignee;
  }

  /** getter: creationDateFrom.
   * @return the creationDateFrom
   */
  public LocalDate getCreationDateFrom() {
    return creationDateFrom;
  }

  /** setter: creationDateFrom.
   * @param creationDateFrom the creationDateFrom to set
   */
  public void setCreationDateFrom(final LocalDate creationDateFrom) {
    this.creationDateFrom = creationDateFrom;
  }

  /** getter: creationDateTo.
   * @return the creationDateTo
   */
  public LocalDate getCreationDateTo() {
    return creationDateTo;
  }

  /** setter: creationDateTo.
   * @param creationDateTo the creationDateTo to set
   */
  public void setCreationDateTo(final LocalDate creationDateTo) {
    this.creationDateTo = creationDateTo;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("status=").append(status);
    sb.append(",assignee=").append(assignee == null ? "null" : assignee.getId());
    sb.append(",creationDateFrom=").append(creationDateFrom);
    sb.append(",creationDateTo=").append(creationDateTo);
    return sb.toString();
  }

}
