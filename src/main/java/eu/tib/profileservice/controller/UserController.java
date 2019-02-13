package eu.tib.profileservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.tib.profileservice.domain.User;
import eu.tib.profileservice.service.UserService;

@Controller
@RequestMapping(value = UserController.BASE_PATH)
public class UserController {

	public static final String BASE_PATH = "/user";
	public static final String PATH_ADD = "/add";
	public static final String PATH_LIST = "/list";
	
	public static final String BASE_URL_TEMPLATE = "user";

	@Autowired
	private UserService userService;

	@ModelAttribute("actionAdd")
	public String populateActionAdd() {
		return BASE_PATH + PATH_ADD;
	}

	@GetMapping("**")
	public String index() {
		return "redirect:" + BASE_PATH + PATH_LIST;
	}

	@GetMapping(PATH_LIST)
	public String list(Model model) {
		final List<User> users = userService.findAll();
		model.addAttribute("users", users);
		return BASE_URL_TEMPLATE + "/list";
	}

	@GetMapping(PATH_ADD)
	public String add(final User user, Model model) {
		return BASE_URL_TEMPLATE + "/create";
	}

	@RequestMapping(value = PATH_ADD, params = { "save" })
	public String addUser(final User user, final Model model, final BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return BASE_URL_TEMPLATE + "/create";
		}
		userService.create(user);
		return "redirect:" + BASE_PATH;
	}

}
