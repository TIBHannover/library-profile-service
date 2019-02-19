package eu.tib.profileservice.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = {
		@UniqueConstraint(columnNames = { Category.COLUMN_NAME_TITLE, Category.COLUMN_NAME_INSTITUTION }) })
public class Category {

	public static final String COLUMN_NAME_TITLE = "category";
	public static final String COLUMN_NAME_INSTITUTION = "institution";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = COLUMN_NAME_TITLE, nullable = false)
	private String category;
	@Column(name = COLUMN_NAME_INSTITUTION, nullable = false)
	private String institution;

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
	 * @return the institution
	 */
	public String getInstitution() {
		return institution;
	}

	/**
	 * @param institution the institution to set
	 */
	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(category);
		sb.append("(");
		sb.append(institution);
		sb.append(")");
		return sb.toString();
	}

}
