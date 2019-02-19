package eu.tib.profileservice.domain;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class DocumentMetadata {

	private static final String TABLE_NAME_ISBNS = "document_metadata_isbns";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String title;
	private String remainderOfTitle;
	private String author;
	@ElementCollection
	@CollectionTable(name = TABLE_NAME_ISBNS)
	private List<String> isbns;
	private String description;

	@ManyToMany
	private List<Category> categories;

	/**
	 * @return the remainderOfTitle
	 */
	public String getRemainderOfTitle() {
		return remainderOfTitle;
	}

	/**
	 * @param remainderOfTitle the remainderOfTitle to set
	 */
	public void setRemainderOfTitle(String remainderOfTitle) {
		this.remainderOfTitle = remainderOfTitle;
	}

	/**
	 * @return the isbns
	 */
	public List<String> getIsbns() {
		return isbns;
	}

	/**
	 * @param isbns the isbns to set
	 */
	public void setIsbns(List<String> isbns) {
		this.isbns = isbns;
	}

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
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
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

	/**
	 * @return the categories
	 */
	public List<Category> getCategories() {
		return categories;
	}

	/**
	 * @param categories the categories to set
	 */
	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

}
