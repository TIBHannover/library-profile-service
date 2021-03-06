package eu.tib.profileservice.domain;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.UniqueConstraint;

@Entity(name = DocumentMetadata.ENTITY_NAME)
public class DocumentMetadata {

  public static final String ENTITY_NAME = "document_metadata";
  private static final String TABLE_NAME_AUTHORS = "document_metadata_authors";
  private static final String TABLE_NAME_ISBNS = "document_metadata_isbns";
  private static final String TABLE_NAME_DDC_CLASSES = "document_metadata_ddcs";
  private static final String TABLE_NAME_FORM_KEYWORDS = "document_metadata_form_keywords";
  private static final String TABLE_NAME_BIBLIOGRAPHY_NUMBER =
      "document_metadata_bibliography_number";
  public static final String TABLE_NAME_INVENTORY_URIS = "document_metadata_inventory_uris";
  public static final String JOIN_TABLE_COLUMN_NAME_DOCUMENTID = "document_metadata_id";
  public static final String JOIN_TABLE_COLUMN_NAME_URI = "uri";
  public static final String JOIN_TABLE_COLUMN_NAME_TITLE = "title";
  /** column name isbns. */
  public static final String COLUMN_NAME_ISBNS = "isbns";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(length = 1024)
  private String title;
  @Column(length = 1024)
  private String remainderOfTitle;

  @ElementCollection
  @Column(length = 1024)
  @CollectionTable(name = TABLE_NAME_AUTHORS)
  private List<String> authors;

  private String publisher;
  private String placeOfPublication;
  private String dateOfPublication;

  @Column(length = 1024)
  private String physicalDescription;

  @Column(length = 1024)
  private String series;

  private String edition;

  @Column(length = 1024)
  private String termsOfAvailability;

  private String formOfProduct;

  @ElementCollection
  @CollectionTable(name = TABLE_NAME_BIBLIOGRAPHY_NUMBER)
  private List<String> bibliographyNumbers;

  @ElementCollection
  @CollectionTable(name = TABLE_NAME_FORM_KEYWORDS)
  private List<String> formKeywords;

  @ElementCollection
  @CollectionTable(name = TABLE_NAME_ISBNS, uniqueConstraints = {@UniqueConstraint(columnNames = {
      COLUMN_NAME_ISBNS})})
  private List<String> isbns;

  @ElementCollection
  @CollectionTable(name = TABLE_NAME_DDC_CLASSES)
  private Set<String> deweyDecimalClassifications;

  /** if an inventory connector is configured: is the document contained in the inventory?
   * when the inventory could not be reached: null. */
  private Boolean containedInInventory;

  private String source;

  /** uris (+title) of the inventory. */
  @ElementCollection
  @MapKeyColumn(name = JOIN_TABLE_COLUMN_NAME_URI, nullable = false, length = 180)
  @Column(name = JOIN_TABLE_COLUMN_NAME_TITLE)
  @CollectionTable(name = TABLE_NAME_INVENTORY_URIS, joinColumns = @JoinColumn(
      name = JOIN_TABLE_COLUMN_NAME_DOCUMENTID))
  private Map<String, String> inventoryUris;

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
  public void setId(final Long id) {
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
  public void setTitle(final String title) {
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
  public void setRemainderOfTitle(final String remainderOfTitle) {
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
  public void setIsbns(final List<String> isbns) {
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
  public void setDeweyDecimalClassifications(final Set<String> deweyDecimalClassifications) {
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
  public void setPublisher(final String publisher) {
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
  public void setTermsOfAvailability(final String termsOfAvailability) {
    this.termsOfAvailability = termsOfAvailability;
  }

  /**
   * setter: authors.
   *
   * @param authors the authors to set
   */
  public void setAuthors(final List<String> authors) {
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
  public void setPlaceOfPublication(final String placeOfPublication) {
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
  public void setDateOfPublication(final String dateOfPublication) {
    this.dateOfPublication = dateOfPublication;
  }

  /**
   * getter: edition.
   *
   * @return the edition
   */
  public String getEdition() {
    return edition;
  }

  /**
   * setter: edition.
   *
   * @param edition the edition to set
   */
  public void setEdition(final String edition) {
    this.edition = edition;
  }

  /**
   * getter: physicalDescription.
   *
   * @return the physicalDescription
   */
  public String getPhysicalDescription() {
    return physicalDescription;
  }

  /**
   * setter: physicalDescription.
   *
   * @param physicalDescription the physicalDescription to set
   */
  public void setPhysicalDescription(final String physicalDescription) {
    this.physicalDescription = physicalDescription;
  }

  /**
   * getter: series.
   *
   * @return the series
   */
  public String getSeries() {
    return series;
  }

  /**
   * setter: series.
   *
   * @param series the series to set
   */
  public void setSeries(final String series) {
    this.series = series;
  }

  /**
   * getter: formOfProduct.
   *
   * @return the formOfProduct
   */
  public String getFormOfProduct() {
    return formOfProduct;
  }

  /**
   * setter: formOfProduct.
   *
   * @param formOfProduct the formOfProduct to set
   */
  public void setFormOfProduct(final String formOfProduct) {
    this.formOfProduct = formOfProduct;
  }

  /**
   * getter: formKeywords.
   *
   * @return the formKeywords
   */
  public List<String> getFormKeywords() {
    return formKeywords;
  }

  /**
   * setter: formKeywords.
   *
   * @param formKeywords the formKeywords to set
   */
  public void setFormKeywords(final List<String> formKeywords) {
    this.formKeywords = formKeywords;
  }

  /** getter: bibliographyNumbers.
   * @return the bibliographyNumbers
   */
  public List<String> getBibliographyNumbers() {
    return bibliographyNumbers;
  }

  /** setter: bibliographyNumbers.
   * @param bibliographyNumbers the bibliographyNumbers to set
   */
  public void setBibliographyNumbers(final List<String> bibliographyNumbers) {
    this.bibliographyNumbers = bibliographyNumbers;
  }

  /** getter: containedInInventory.
   * true, if an inventory connector is configured and the document exists in the inventory
   * when the document is imported.
   * @return the containedInInventory
   */
  public Boolean isContainedInInventory() {
    return containedInInventory;
  }

  /** setter: containedInInventory.
   * @param containedInInventory the containedInInventory to set
   */
  public void setContainedInInventory(final Boolean containedInInventory) {
    this.containedInInventory = containedInInventory;
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

  /** getter: inventoryUris.
   * @return the inventoryUris
   */
  public Map<String, String> getInventoryUris() {
    return inventoryUris;
  }

  /** setter: inventoryUris.
   * @param inventoryUris the inventoryUris to set
   */
  public void setInventoryUris(final Map<String, String> inventoryUris) {
    this.inventoryUris = inventoryUris;
  }

}
