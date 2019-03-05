package eu.tib.profileservice.service;

import eu.tib.profileservice.domain.User;
import java.util.List;

public interface UserService {

  public List<User> findAll();

  public User findByName(final String name);

  public User findById(final Long id);

  public User create(final User user);

  public User update(final User user);

  public void delete(final User user);
}
