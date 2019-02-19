package eu.tib.profileservice.domain;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { User.COLUMN_NAME_NAME }) })
public class User {

	public static final String COLUMN_NAME_NAME = "name";
	
	public static final String JOIN_TABLE_NAME_USERROLE = "user_role";
	public static final String JOIN_TABLE_COLUMN_NAME_USERID = "user_id";
	public static final String JOIN_TABLE_COLUMN_NAME_ROLEID = "role_id";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = COLUMN_NAME_NAME, nullable = false)
	private String name;

	@Column(nullable = false)
	private String password;

	private String initials;

	private String email;

	@ManyToMany
	@JoinTable(name = JOIN_TABLE_NAME_USERROLE, joinColumns = @JoinColumn(name = JOIN_TABLE_COLUMN_NAME_USERID), inverseJoinColumns = @JoinColumn(name = JOIN_TABLE_COLUMN_NAME_ROLEID))
	private List<Role> roles;

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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the initials
	 */
	public String getInitials() {
		return initials;
	}

	/**
	 * @param initials the initials to set
	 */
	public void setInitials(String initials) {
		this.initials = initials;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the roles
	 */
	public List<Role> getRoles() {
		return roles;
	}

	/**
	 * @param roles the roles to set
	 */
	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}


}
