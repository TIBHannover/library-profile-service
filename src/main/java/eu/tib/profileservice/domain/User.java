package eu.tib.profileservice.domain;

import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {User.COLUMN_NAME_NAME})})
public class User {

  public enum Role {
    MANAGE_USERS,
    PROCESS_DOCUMENTS,
    IMPORT_DOCUMENTS
  }

  public static final String COLUMN_NAME_NAME = "name";

  public static final String JOIN_TABLE_NAME_USERROLE = "user_role";
  public static final String JOIN_TABLE_COLUMN_NAME_USERID = "user_id";
  public static final String JOIN_TABLE_COLUMN_NAME_ROLEID = "role_id";

  public static final String JOIN_TABLE_NAME_USERCATEGORY = "user_categories";
  public static final String JOIN_TABLE_COLUMN_NAME_CATEGORYID = "category_id";

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(name = COLUMN_NAME_NAME, nullable = false)
  private String name;

  @Column(nullable = false)
  private String password;

  private String initials;

  private String email;

  @Enumerated(EnumType.STRING)
  @ElementCollection
  @CollectionTable(name = JOIN_TABLE_NAME_USERROLE, joinColumns = @JoinColumn(
      name = JOIN_TABLE_COLUMN_NAME_USERID))
  @Column(name = JOIN_TABLE_COLUMN_NAME_ROLEID)
  private List<Role> roles;

  @OneToMany(orphanRemoval = false)
  @JoinTable(name = JOIN_TABLE_NAME_USERCATEGORY, joinColumns = @JoinColumn(
      name = JOIN_TABLE_COLUMN_NAME_USERID), inverseJoinColumns = @JoinColumn(
          name = JOIN_TABLE_COLUMN_NAME_CATEGORYID))
  private List<Category> categories;

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
   * getter: name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * setter: name.
   * 
   * @param name the name to set
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * getter: password.
   * 
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * setter: password.
   * 
   * @param password the password to set
   */
  public void setPassword(final String password) {
    this.password = password;
  }

  /**
   * getter: initials.
   * 
   * @return the initials
   */
  public String getInitials() {
    return initials;
  }

  /**
   * setter: initials.
   * 
   * @param initials the initials to set
   */
  public void setInitials(final String initials) {
    this.initials = initials;
  }

  /**
   * getter: email.
   * 
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * setter: email.
   * 
   * @param email the email to set
   */
  public void setEmail(final String email) {
    this.email = email;
  }

  /**
   * getter: roles.
   * 
   * @return the roles
   */
  public List<Role> getRoles() {
    return roles;
  }

  /**
   * setter: roles.
   * 
   * @param roles the roles to set
   */
  public void setRoles(final List<Role> roles) {
    this.roles = roles;
  }

  /**
   * getter: categories.
   * 
   * @return the categories
   */
  public List<Category> getCategories() {
    return categories;
  }

  /**
   * setter: categories.
   * 
   * @param categories the categories to set
   */
  public void setCategories(final List<Category> categories) {
    this.categories = categories;
  }


}
