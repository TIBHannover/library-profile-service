package eu.tib.profileservice.controller;

import eu.tib.profileservice.domain.Document;
import java.util.List;

/**
 * Wrapper object that will hold a list of {@link Document}s.
 * => thymeleaf needs such data transfer objects for lists...
 */
public class DocumentListDto {

  private List<Document> documents;
  private List<Boolean> selected;

  /**
   * Constructor of {@link DocumentListDto}.
   */
  public DocumentListDto() {
  }

  /**
   * Constructor of {@link DocumentListDto}.
   * @param documents documents
   */
  public DocumentListDto(final List<Document> documents) {
    this.documents = documents;
  }

  /** getter: documents.
   * @return the documents
   */
  public List<Document> getDocuments() {
    return documents;
  }

  /** setter: documents.
   * @param documents the documents to set
   */
  public void setDocuments(final List<Document> documents) {
    this.documents = documents;
  }

  /** getter: selected.
   * @return the selected
   */
  public List<Boolean> getSelected() {
    return selected;
  }

  /** setter: selected.
   * @param selected the selected to set
   */
  public void setSelected(final List<Boolean> selected) {
    this.selected = selected;
  }
}
