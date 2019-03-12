package eu.tib.profileservice.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Entity that describes an import-filter for the document import. When the condition is satisfied,
 * then the action will be processed during the document import.
 */
@Entity(name = ImportFilter.ENTITY_NAME)
public class ImportFilter {

  public static final String ENTITY_NAME = "import_filter";

  /**
   * Type of the condition of an {@link ImportFilter}.
   */
  public enum ConditionType {
    FORM_KEYWORD,
    CATEGORY
  }

  /**
   * Action of an {@link ImportFilter} that will be processed when the condition matches.
   */
  public enum Action {
    IGNORE
  }

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Enumerated(EnumType.STRING)
  private ConditionType conditionType;

  @Column(length = 1024)
  private String condition;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Action action;

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
   * getter: conditionType.
   * 
   * @return the conditionType
   */
  public ConditionType getConditionType() {
    return conditionType;
  }

  /**
   * setter: conditionType.
   * 
   * @param conditionType the conditionType to set
   */
  public void setConditionType(final ConditionType conditionType) {
    this.conditionType = conditionType;
  }

  /**
   * getter: condition.
   * 
   * @return the condition
   */
  public String getCondition() {
    return condition;
  }

  /**
   * setter: condition.
   * 
   * @param condition the condition to set
   */
  public void setCondition(final String condition) {
    this.condition = condition;
  }

  /**
   * getter: action.
   * 
   * @return the action
   */
  public Action getAction() {
    return action;
  }

  /**
   * setter: action.
   * 
   * @param action the action to set
   */
  public void setAction(final Action action) {
    this.action = action;
  }
}
