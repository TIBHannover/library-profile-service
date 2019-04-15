package eu.tib.profileservice.service;

import eu.tib.profileservice.domain.User;
import java.util.List;
import java.util.Map;

public interface UserService {

  List<User> findAll();

  User findByName(String name);

  User findById(Long id);

  User create(User user);

  User update(User user);

  void delete(User user);

  Map<Long, User> determineCategoryIdsAssignedToUsers();
}
