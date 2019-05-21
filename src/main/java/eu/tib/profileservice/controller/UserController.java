package eu.tib.profileservice.controller;

import static eu.tib.profileservice.controller.HomeController.ATTRIBUTE_INFO_MESSAGE;
import static eu.tib.profileservice.controller.HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE;
import static eu.tib.profileservice.controller.HomeController.INFO_MESSAGE_TYPE_ERROR;
import static eu.tib.profileservice.controller.HomeController.INFO_MESSAGE_TYPE_SUCCESS;

import eu.tib.profileservice.domain.Category;
import eu.tib.profileservice.domain.User;
import eu.tib.profileservice.domain.User.Role;
import eu.tib.profileservice.service.CategoryService;
import eu.tib.profileservice.service.UserService;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value = UserController.BASE_PATH)
public class UserController {

  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

  protected static final String CODE_MESSAGE_USER_NOTFOUND = "message.user.notfound";
  protected static final String CODE_MESSAGE_USER_DELETED = "message.user.delete.success";
  protected static final String CODE_MESSAGE_USER_CREATED = "message.user.create.success";
  protected static final String CODE_MESSAGE_USER_UPDATED = "message.user.update.success";

  public static final String BASE_PATH = "/user";
  public static final String PATH_ADD = "/add";
  public static final String PATH_EDIT = "/edit";
  public static final String PATH_SAVE = "/save";
  public static final String PATH_DELETE = "/delete";
  public static final String PATH_LIST = "/list";
  public static final String PATH_SHOW = "/show";

  public static final String TEMPLATE_CREATE_OR_EDIT = "/createOrEdit";
  public static final String TEMPLATE_LIST = "/list";
  public static final String TEMPLATE_SHOW = "/show";

  public static final String METHOD_CREATE = "create";
  public static final String METHOD_EDIT = "edit";
  public static final String METHOD_REFRESH_CREATE = "refreshCreate";
  public static final String METHOD_REFRESH_EDIT = "refreshEdit";

  public static final String BASE_URL_TEMPLATE = "user";

  @Autowired
  private UserService userService;

  @Autowired
  private CategoryService categoryService;

  @ModelAttribute("actionSave")
  public String populateActionAdd() {
    return BASE_PATH + PATH_SAVE;
  }

  @ModelAttribute("methodCreate")
  public String populateMethodCreate() {
    return METHOD_CREATE;
  }

  @ModelAttribute("methodEdit")
  public String populateMethodEdit() {
    return METHOD_EDIT;
  }

  @ModelAttribute("methodRefreshCreate")
  public String populateMethodRefreshCreate() {
    return METHOD_REFRESH_CREATE;
  }

  @ModelAttribute("methodRefreshEdit")
  public String populateMethodRefreshEdit() {
    return METHOD_REFRESH_EDIT;
  }

  @ModelAttribute("availableRoles")
  public User.Role[] populateAvailableRoles() {
    return User.Role.values();
  }

  @ModelAttribute("availableCategories")
  public List<Category> populateAvailableCategories() {
    return categoryService.findAll();
  }

  @ModelAttribute("categoryIdsAssignedToUsers")
  public Map<Long, User> populateCategoryIdsAssignedToUsers() {
    return userService.determineCategoryIdsAssignedToUsers();
  }

  /**
   * Populate the grouped categories. (parent group and associated child categories)
   * @return parent group and associated child categories
   */
  @ModelAttribute("availableGroupedCategories")
  public Map<Category, List<Category>> populateAvailableGroupedCategories() {
    List<Category> all = categoryService.findAll();
    List<Category> main = all.stream().filter(c -> all.stream().allMatch(a -> !includes(a, c)))
        .collect(Collectors
        .toList());
    Map<Category, List<Category>> result = new TreeMap<Category, List<Category>>(
        new Comparator<Category>() {
          @Override
          public int compare(final Category arg0, final Category arg1) {
            return arg0.getCategory().compareTo(arg1.getCategory());
          }
        });
    for (Category category : main) {
      List<Category> children = all.stream().filter(c -> includes(category, c)).collect(Collectors
          .toList());
      result.put(category, children);
    }
    return result;
  }

  private String removeTrailingZeros(final String category) {
    String result = category;
    while (result.length() > 1 && result.charAt(result.length() - 1) == '0') {
      result = result.substring(0, result.length() - 1);
    }
    return result;
  }

  /**
   * Check if the given parent-category is a parent of the given child-category.
   * @param parent parent-category
   * @param child child-category
   * @return true, if parent is parent of child
   */
  private boolean includes(final Category parent, final Category child) {
    if (Category.Type.DDC.equals(parent.getType()) && Category.Type.DDC.equals(child.getType())) {
      String parentCategory = removeTrailingZeros(parent.getCategory());
      String childCategory = removeTrailingZeros(child.getCategory());
      if (childCategory.length() > parentCategory.length()) {
        String adjustedCategory = childCategory.substring(0, parentCategory.length());
        return parentCategory.equals(adjustedCategory);
      }
    }
    return false;
  }

  /**
   * Check, if any of the given categories is assigned to the given user.
   * @param user user
   * @param categories categories
   * @return true, if at least one of the categories is assigned to the user
   */
  public static boolean hasAnyCategory(final User user, final List<Category> categories) {
    if (user != null && user.getCategories() != null && categories != null) {
      for (Category category : user.getCategories()) {
        for (Category category2 : categories) {
          if (category.getId().equals(category2.getId())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @GetMapping("**")
  public String index() {
    return "redirect:" + BASE_PATH + PATH_LIST;
  }

  @GetMapping(PATH_LIST)
  public String list(final Model model) {
    final List<User> users = userService.findAll();
    model.addAttribute("users", users);
    return BASE_URL_TEMPLATE + TEMPLATE_LIST;
  }

  /**
   * Refresh 'edit user' with the given user.
   * @param user user
   * @param model model
   * @return template
   */
  @RequestMapping(value = PATH_SAVE, params = {METHOD_REFRESH_EDIT}, method = RequestMethod.POST)
  public String refreshEditUser(final User user, final Model model) {
    model.addAttribute("edit", true);
    model.addAttribute("user", user);
    return BASE_URL_TEMPLATE + TEMPLATE_CREATE_OR_EDIT;
  }

  /**
   * Refresh 'add user' with the given user.
   * @param user user
   * @param model model
   * @return template
   */
  @RequestMapping(value = PATH_SAVE, params = {METHOD_REFRESH_CREATE}, method = RequestMethod.POST)
  public String refreshAddUser(final User user, final Model model) {
    model.addAttribute("user", user);
    return BASE_URL_TEMPLATE + TEMPLATE_CREATE_OR_EDIT;
  }

  @GetMapping(PATH_ADD)
  public String add(final User user, final Model model) {
    List<Role> defaultRoles = Arrays.asList(Role.MANAGE_USERS, Role.PROCESS_DOCUMENTS);
    user.setRoles(defaultRoles);
    model.addAttribute("user", user);
    return BASE_URL_TEMPLATE + TEMPLATE_CREATE_OR_EDIT;
  }

  @GetMapping(PATH_EDIT)
  @RequestMapping(value = {PATH_EDIT + "/{userId}"}, method = RequestMethod.GET)
  public String edit(@PathVariable("userId") final Long userId, final Model model,
      final RedirectAttributes redirectAttrs) {
    User user = userService.findById(userId);
    if (user == null) {
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_USER_NOTFOUND);
      return "redirect:" + BASE_PATH + PATH_LIST;
    }
    model.addAttribute("edit", true);
    model.addAttribute("user", user);
    return BASE_URL_TEMPLATE + TEMPLATE_CREATE_OR_EDIT;
  }

  @RequestMapping(value = {PATH_SHOW + "/{userId}"}, method = RequestMethod.GET)
  public String show(@PathVariable("userId") final Long userId, final Model model,
      final RedirectAttributes redirectAttrs) {
    User user = userService.findById(userId);
    if (user == null) {
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_USER_NOTFOUND);
      return "redirect:" + BASE_PATH + PATH_LIST;
    }
    model.addAttribute("user", user);
    return BASE_URL_TEMPLATE + TEMPLATE_SHOW;
  }

  @RequestMapping(value = PATH_SAVE, params = {METHOD_CREATE}, method = RequestMethod.POST)
  public String addUser(final User user, final Model model, final RedirectAttributes redirectAttrs,
      final BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return BASE_URL_TEMPLATE + TEMPLATE_CREATE_OR_EDIT;
    }
    userService.create(user);
    redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_SUCCESS);
    redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_USER_CREATED);
    return "redirect:" + BASE_PATH + PATH_LIST;
  }

  @RequestMapping(value = PATH_SAVE, params = {METHOD_EDIT}, method = RequestMethod.POST)
  public String updateUser(final User user, final Model model,
      final RedirectAttributes redirectAttrs, final BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return BASE_URL_TEMPLATE + TEMPLATE_CREATE_OR_EDIT;
    }
    userService.update(user);
    redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_SUCCESS);
    redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_USER_UPDATED);
    return "redirect:" + BASE_PATH + PATH_LIST;
  }

  @RequestMapping(value = PATH_DELETE, method = RequestMethod.POST)
  public String deleteUser(final User user, final Model model,
      final RedirectAttributes redirectAttrs, final BindingResult bindingResult) {
    userService.delete(user);
    redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_SUCCESS);
    redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_USER_DELETED);
    return "redirect:" + BASE_PATH + PATH_LIST;
  }
}
