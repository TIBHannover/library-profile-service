package eu.tib.profileservice.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity(name=Category.ENTITY_NAME)
@Table(uniqueConstraints = {
		@UniqueConstraint(columnNames = { Category.COLUMN_NAME_CATEGORY, Category.COLUMN_NAME_TYPE }) })
public class Category {
	
	public enum Type {
		DDC
	}

	public static final String ENTITY_NAME = "Category";
	public static final String COLUMN_NAME_CATEGORY = "category";
	public static final String COLUMN_NAME_TYPE = "type";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = COLUMN_NAME_CATEGORY, nullable = false)
	private String category;
	@Enumerated(EnumType.STRING)
	@Column(name = COLUMN_NAME_TYPE, nullable = false)
	private Type type;
	
	private String description;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(category);
		if (description != null) {
			sb.append(" - ").append(description);
		}
		sb.append(" (");
		sb.append(type);
		sb.append(")");
		return sb.toString();
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

}
