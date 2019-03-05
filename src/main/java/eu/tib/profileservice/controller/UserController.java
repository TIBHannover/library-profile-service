package eu.tib.profileservice.controller;

import static eu.tib.profileservice.controller.HomeController.ATTRIBUTE_INFO_MESSAGE;
import static eu.tib.profileservice.controller.HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE;
import static eu.tib.profileservice.controller.HomeController.INFO_MESSAGE_TYPE_ERROR;
import static eu.tib.profileservice.controller.HomeController.INFO_MESSAGE_TYPE_SUCCESS;

import eu.tib.profileservice.domain.User;
import eu.tib.profileservice.service.UserService;
import java.util.List;
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

  public static final String BASE_URL_TEMPLATE = "user";

  @Autowired
  private UserService userService;

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

  @ModelAttribute("availableRoles")
  public User.Role[] populateAvailableRoles() {
    return User.Role.values();
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

  @GetMapping(PATH_ADD)
  public String add(final User user, final Model model) {
    return BASE_URL_TEMPLATE + TEMPLATE_CREATE_OR_EDIT;
  }

  @GetMapping(PATH_EDIT)
  @RequestMapping({PATH_EDIT + "/{userId}"})
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

  @RequestMapping({PATH_SHOW + "/{userId}"})
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
