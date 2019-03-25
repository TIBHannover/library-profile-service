package eu.tib.profileservice.service;

import eu.tib.profileservice.domain.User;
import java.util.List;

public interface UserService {

  List<User> findAll();

  User findByName(final String name);

  User findById(final Long id);

  User create(final User user);

  User update(final User user);

  void delete(final User user);
}
