package eu.tib.profileservice.service;

import eu.tib.profileservice.domain.Category;
import eu.tib.profileservice.domain.User;
import eu.tib.profileservice.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

  private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public List<User> findAll() {
    return userRepository.findAll();
  }

  @Override
  @Transactional
  public User create(final User user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    updateCategoriesOfOtherUsers(user);
    return userRepository.save(user);
  }

  @Transactional
  private void updateCategoriesOfOtherUsers(final User user) {
    if (user.getCategories() == null) {
      return;
    }
    Map<Long, User> categoryIds = determineCategoryIdsAssignedToUsers();
    for (Category category : user.getCategories()) {
      User otherUser = categoryIds.get(category.getId());
      if (otherUser != null && !otherUser.getId().equals(user.getId())) {
        removeCategory(otherUser, category.getId());
        LOG.debug("Update other user {}: remove category {}", user.getName(), category.getId());
        userRepository.save(otherUser);
      }
    }
  }

  private void removeCategory(final User user, final Long categoryId) {
    List<Category> newCategories = new ArrayList<Category>(user.getCategories());
    for (Iterator<Category> iterator = newCategories.iterator(); iterator.hasNext();) {
      Category category = iterator.next();
      if (category.getId().equals(categoryId)) {
        iterator.remove();
        user.setCategories(newCategories);
        break;
      }
    }
  }

  @Transactional
  @Override
  public User update(final User user) {
    final User currentUser = findById(user.getId());
    if (currentUser == null) {
      throw new IllegalArgumentException("user does not exist");
    }
    if (!currentUser.getPassword().equals(user.getPassword())) {
      user.setPassword(passwordEncoder.encode(user.getPassword()));
      LOG.debug("Change password of user {}", user.getName());
    }
    updateCategoriesOfOtherUsers(user);
    return userRepository.save(user);
  }

  @Transactional(readOnly = true)
  @Override
  public User findByName(final String name) {
    return userRepository.findByName(name);
  }

  @Transactional
  @Override
  public void delete(final User user) {
    userRepository.delete(user);
  }

  @Transactional(readOnly = true)
  @Override
  public User findById(final Long id) {
    if (id == null) {
      return null;
    }
    try {
      return userRepository.findById(id).get();
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  @Transactional(readOnly = true)
  @Override
  public Map<Long, User> determineCategoryIdsAssignedToUsers() {
    List<User> users = userRepository.findAll();
    Map<Long, User> result = new HashMap<Long, User>();
    for (User user : users) {
      List<Category> categories = user.getCategories();
      if (categories != null) {
        for (Category category : categories) {
          result.put(category.getId(), user);
        }
      }
    }
    return result;
  }

}
