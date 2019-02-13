package eu.tib.profileservice.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.DocumentAssignment;
import eu.tib.profileservice.domain.User;
import eu.tib.profileservice.service.DocumentService;
import eu.tib.profileservice.service.UserService;

@Controller
@RequestMapping(value = DocumentController.BASE_PATH)
public class DocumentController {
	
	private static final Logger LOG = LoggerFactory.getLogger(DocumentController.class);
	
	public static final String BASE_PATH = "/document";
	public static final String PATH_LIST = "/list";
	public static final String PATH_SHOW = "/show";
	public static final String PATH_ASSIGN = "/assign";
	
	public static final String BASE_URL_TEMPLATE = "document";
	
	@Autowired
	private DocumentService documentService;
	@Autowired
	private UserService userService;

	@ModelAttribute("users")
	public List<User> populateUsers() {
		return userService.findAll();
	}

	@ModelAttribute("actionAssign")
	public String populateActionAssign() {
		return BASE_PATH + PATH_ASSIGN;
	}
	
	@GetMapping("**")
	public String index() {
		return "redirect:" + BASE_PATH + PATH_LIST;
	}
	
	@GetMapping(PATH_LIST)
	public String list(Model model) {
		final List<Document> documents = documentService.findAll();
		model.addAttribute("documents", documents);
		return BASE_URL_TEMPLATE + "/list";
	}
	
	@GetMapping(PATH_SHOW)
	public String show(Model model, @RequestParam final Long id) {
		DocumentAssignment assignment = documentService.retrieveAssignmentByDocumentId(id);
		final Document document = documentService.findById(id);
		if (assignment == null) {
			assignment = new DocumentAssignment();
			assignment.setDocument(document);
		}
		model.addAttribute("assignment", assignment);
		model.addAttribute("document", document);
		return BASE_URL_TEMPLATE + "/show";
	}
	
	@RequestMapping(value = PATH_ASSIGN, params = { "assign" })
	public String assignDocument(final DocumentAssignment assignment, final Model model, final BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return BASE_URL_TEMPLATE + "/show";
		}
		LOG.debug("Assign document '" + assignment.getDocument().getId() + "' to user '" + assignment.getAssignee().getName() + "'");
		documentService.assignToUser(assignment.getDocument(), assignment.getAssignee());
		return "redirect:" + BASE_PATH + PATH_SHOW + "?id=" + assignment.getDocument().getId();
	}


}
