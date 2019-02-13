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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getInitials() {
		return initials;
	}

	public void setInitials(String initials) {
		this.initials = initials;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
}
