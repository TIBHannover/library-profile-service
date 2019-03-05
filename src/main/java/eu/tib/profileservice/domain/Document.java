package eu.tib.profileservice.domain;

import java.time.LocalDateTime;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class Document {

  public static final String COLUMN_NAME_CREATION_DATE = "creation_date";

  public enum Status {
    ACCEPTED, REJECTED, IN_PROGRESS, IGNORED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @OneToOne(cascade = CascadeType.ALL)
  private DocumentMetadata metadata;

  @Enumerated(EnumType.STRING)
  private Status status;

  @ManyToOne
  private User assignee;

  @Column(name = COLUMN_NAME_CREATION_DATE, nullable = false)
  private LocalDateTime creationDateUtc;

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("id=").append(id);
    sb.append(",metadata=").append(metadata == null ? "null" : metadata.getId());
    sb.append(",status=").append(status);
    sb.append(",assignee=").append(assignee == null ? "null" : assignee.getId());
    sb.append(",creationDateUtc=").append(creationDateUtc);
    return sb.toString();
  }

  /**
   * getter: id.
   * 
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * setter: id.
   * 
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * getter: metadata.
   * 
   * @return the metadata
   */
  public DocumentMetadata getMetadata() {
    return metadata;
  }

  /**
   * setter: metadata.
   * 
   * @param metadata the metadata to set
   */
  public void setMetadata(DocumentMetadata metadata) {
    this.metadata = metadata;
  }

  /**
   * getter: status.
   * 
   * @return the status
   */
  public Status getStatus() {
    return status;
  }

  /**
   * setter: status.
   * 
   * @param status the status to set
   */
  public void setStatus(Status status) {
    this.status = status;
  }

  /**
   * getter: assignee.
   * 
   * @return the assignee
   */
  public User getAssignee() {
    return assignee;
  }

  /**
   * setter: assignee.
   * 
   * @param assignee the assignee to set
   */
  public void setAssignee(User assignee) {
    this.assignee = assignee;
  }

  /**
   * getter: creationDateUtc.
   * 
   * @return the creationDateUtc
   */
  public LocalDateTime getCreationDateUtc() {
    return creationDateUtc;
  }

  /**
   * setter: creationDateUtc.
   * 
   * @param creationDateUtc the creationDateUtc to set
   */
  public void setCreationDateUtc(LocalDateTime creationDateUtc) {
    this.creationDateUtc = creationDateUtc;
  }
}
