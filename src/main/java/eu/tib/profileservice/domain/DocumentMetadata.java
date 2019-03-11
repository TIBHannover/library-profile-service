package eu.tib.profileservice.domain;

import java.util.List;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class DocumentMetadata {

  private static final String TABLE_NAME_AUTHORS = "document_metadata_authors";
  private static final String TABLE_NAME_ISBNS = "document_metadata_isbns";
  private static final String TABLE_NAME_DDC_CLASSES = "document_metadata_ddcs";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(length = 1024)
  private String title;
  @Column(length = 1024)
  private String remainderOfTitle;

  @ElementCollection
  @CollectionTable(name = TABLE_NAME_AUTHORS)
  private List<String> authors;

  private String publisher;
  private String placeOfPublication;
  private String dateOfPublication;

  @Column(length = 1024)
  private String termsOfAvailability;

  @ElementCollection
  @CollectionTable(name = TABLE_NAME_ISBNS)
  private List<String> isbns;

  @ElementCollection
  @CollectionTable(name = TABLE_NAME_DDC_CLASSES)
  private Set<String> deweyDecimalClassifications;

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
   * getter: title.
   * 
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * setter: title.
   * 
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * getter: remainderOfTitle.
   * 
   * @return the remainderOfTitle
   */
  public String getRemainderOfTitle() {
    return remainderOfTitle;
  }

  /**
   * setter: remainderOfTitle.
   * 
   * @param remainderOfTitle the remainderOfTitle to set
   */
  public void setRemainderOfTitle(String remainderOfTitle) {
    this.remainderOfTitle = remainderOfTitle;
  }

  /**
   * getter: isbns.
   * 
   * @return the isbns
   */
  public List<String> getIsbns() {
    return isbns;
  }

  /**
   * setter: isbns.
   * 
   * @param isbns the isbns to set
   */
  public void setIsbns(List<String> isbns) {
    this.isbns = isbns;
  }

  /**
   * getter: deweyDecimalClassifications.
   * 
   * @return the deweyDecimalClassifications
   */
  public Set<String> getDeweyDecimalClassifications() {
    return deweyDecimalClassifications;
  }

  /**
   * setter: deweyDecimalClassifications.
   * 
   * @param deweyDecimalClassifications the deweyDecimalClassifications to set
   */
  public void setDeweyDecimalClassifications(Set<String> deweyDecimalClassifications) {
    this.deweyDecimalClassifications = deweyDecimalClassifications;
  }

  /**
   * getter: publisher.
   * 
   * @return the publisher
   */
  public String getPublisher() {
    return publisher;
  }

  /**
   * setter: publisher.
   * 
   * @param publisher the publisher to set
   */
  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  /**
   * getter: termsOfAvailability.
   * 
   * @return the termsOfAvailability
   */
  public String getTermsOfAvailability() {
    return termsOfAvailability;
  }

  /**
   * setter: termsOfAvailability.
   * 
   * @param termsOfAvailability the termsOfAvailability to set
   */
  public void setTermsOfAvailability(String termsOfAvailability) {
    this.termsOfAvailability = termsOfAvailability;
  }

  /**
   * setter: authors.
   * 
   * @param authors the authors to set
   */
  public void setAuthors(List<String> authors) {
    this.authors = authors;
  }

  /**
   * getter: authors.
   * 
   * @return the authors
   */
  public List<String> getAuthors() {
    return authors;
  }

  /**
   * getter: placeOfPublication.
   * 
   * @return the placeOfPublication
   */
  public String getPlaceOfPublication() {
    return placeOfPublication;
  }

  /**
   * setter: placeOfPublication.
   * 
   * @param placeOfPublication the placeOfPublication to set
   */
  public void setPlaceOfPublication(String placeOfPublication) {
    this.placeOfPublication = placeOfPublication;
  }

  /**
   * getter: dateOfPublication.
   * 
   * @return the dateOfPublication
   */
  public String getDateOfPublication() {
    return dateOfPublication;
  }

  /**
   * setter: dateOfPublication.
   * 
   * @param dateOfPublication the dateOfPublication to set
   */
  public void setDateOfPublication(String dateOfPublication) {
    this.dateOfPublication = dateOfPublication;
  }

}
