package eu.tib.profileservice.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Statistics of an import.
 */
@Entity(name = DocumentImportStatistics.ENTITY_NAME)
public class DocumentImportStatistics {

  /** Name of this entity. */
  public static final String ENTITY_NAME = "document_import_statistics";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private LocalDateTime start;
  private LocalDateTime end;

  private String source;
  private LocalDate fromDate;
  private LocalDate toDate;

  private boolean errorInInstitutionConnector;
  private boolean errorInInventoryConnector;

  private int nrRetrieved;
  private int nrExists;
  private int nrUpdated;
  private int nrInvalid;
  private int nrIgnored;
  private int nrImported;

  /**
   * Add the given amount.
   * @param amount amount to add
   */
  public void addNrUpdated(final int amount) {
    this.nrUpdated += amount;
  }

  /**
   * Add the given amount.
   * @param amount amount to add
   */
  public void addNrRetrieved(final int amount) {
    this.nrRetrieved += amount;
  }

  /**
   * Add the given amount.
   * @param amount amount to add
   */
  public void addNrExists(final int amount) {
    this.nrExists += amount;
  }

  /**
   * Add the given amount.
   * @param amount amount to add
   */
  public void addNrInvalid(final int amount) {
    this.nrInvalid += amount;
  }

  /**
   * Add the given amount.
   * @param amount amount to add
   */
  public void addNrIgnored(final int amount) {
    this.nrIgnored += amount;
  }

  /**
   * Add the given amount.
   * @param amount amount to add
   */
  public void addNrImported(final int amount) {
    this.nrImported += amount;
  }

  /** getter: start.
   * @return the start
   */
  public LocalDateTime getStart() {
    return start;
  }

  /** setter: start.
   * @param start the start to set
   */
  public void setStart(final LocalDateTime start) {
    this.start = start;
  }

  /** getter: end.
   * @return the end
   */
  public LocalDateTime getEnd() {
    return end;
  }

  /** setter: end.
   * @param end the end to set
   */
  public void setEnd(final LocalDateTime end) {
    this.end = end;
  }

  /** getter: source.
   * @return the source
   */
  public String getSource() {
    return source;
  }

  /** setter: source.
   * @param source the source to set
   */
  public void setSource(final String source) {
    this.source = source;
  }

  /** getter: fromDate.
   * @return the fromDate
   */
  public LocalDate getFromDate() {
    return fromDate;
  }

  /** setter: fromDate.
   * @param fromDate the fromDate to set
   */
  public void setFromDate(final LocalDate fromDate) {
    this.fromDate = fromDate;
  }

  /** getter: toDate.
   * @return the toDate
   */
  public LocalDate getToDate() {
    return toDate;
  }

  /** setter: toDate.
   * @param toDate the toDate to set
   */
  public void setToDate(final LocalDate toDate) {
    this.toDate = toDate;
  }

  /** getter: errorInInstitutionConnector.
   * @return the errorInInstitutionConnector
   */
  public boolean isErrorInInstitutionConnector() {
    return errorInInstitutionConnector;
  }

  /** setter: errorInInstitutionConnector.
   * @param errorInInstitutionConnector the errorInInstitutionConnector to set
   */
  public void setErrorInInstitutionConnector(final boolean errorInInstitutionConnector) {
    this.errorInInstitutionConnector = errorInInstitutionConnector;
  }

  /** getter: errorInInventoryConnector.
   * @return the errorInInventoryConnector
   */
  public boolean isErrorInInventoryConnector() {
    return errorInInventoryConnector;
  }

  /** setter: errorInInventoryConnector.
   * @param errorInInventoryConnector the errorInInventoryConnector to set
   */
  public void setErrorInInventoryConnector(final boolean errorInInventoryConnector) {
    this.errorInInventoryConnector = errorInInventoryConnector;
  }

  /** getter: nrRetrieved.
   * @return the nrRetrieved
   */
  public int getNrRetrieved() {
    return nrRetrieved;
  }

  /** setter: nrRetrieved.
   * @param nrRetrieved the nrRetrieved to set
   */
  public void setNrRetrieved(final int nrRetrieved) {
    this.nrRetrieved = nrRetrieved;
  }

  /** getter: nrExists.
   * @return the nrExists
   */
  public int getNrExists() {
    return nrExists;
  }

  /** setter: nrExists.
   * @param nrExists the nrExists to set
   */
  public void setNrExists(final int nrExists) {
    this.nrExists = nrExists;
  }

  /** getter: nrInvalid.
   * @return the nrInvalid
   */
  public int getNrInvalid() {
    return nrInvalid;
  }

  /** setter: nrInvalid.
   * @param nrInvalid the nrInvalid to set
   */
  public void setNrInvalid(final int nrInvalid) {
    this.nrInvalid = nrInvalid;
  }

  /** getter: nrIgnored.
   * @return the nrIgnored
   */
  public int getNrIgnored() {
    return nrIgnored;
  }

  /** setter: nrIgnored.
   * @param nrIgnored the nrIgnored to set
   */
  public void setNrIgnored(final int nrIgnored) {
    this.nrIgnored = nrIgnored;
  }

  /** getter: nrImported.
   * @return the nrImported
   */
  public int getNrImported() {
    return nrImported;
  }

  /** setter: nrImported.
   * @param nrImported the nrImported to set
   */
  public void setNrImported(final int nrImported) {
    this.nrImported = nrImported;
  }

  /** getter: id.
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /** setter: id.
   * @param id the id to set
   */
  public void setId(final Long id) {
    this.id = id;
  }

  /** getter: nrUpdated.
   * @return the nrUpdated
   */
  public int getNrUpdated() {
    return nrUpdated;
  }

  /** setter: nrUpdated.
   * @param nrUpdated the nrUpdated to set
   */
  public void setNrUpdated(final int nrUpdated) {
    this.nrUpdated = nrUpdated;
  }

}
