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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.DocumentAssignment;
import eu.tib.profileservice.domain.User;
import eu.tib.profileservice.service.DocumentService;
import eu.tib.profileservice.service.UserService;

@Controller
@RequestMapping(value = DocumentController.BASE_PATH)
public class DocumentController {
	
	private static final Logger LOG = LoggerFactory.getLogger(DocumentController.class);
	
	public static final String ATTRIBUTE_INFO_MESSAGE = "infoMessage";
	public static final String ATTRIBUTE_INFO_MESSAGE_TYPE = "infoMessageType";
	public static final String INFO_MESSAGE_TYPE_SUCCESS = "success";
	public static final String INFO_MESSAGE_TYPE_ERROR = "error";
	public static final String CODE_MESSAGE_DOCUMENT_ACCEPTED = "message.document.accepted";
	public static final String CODE_MESSAGE_DOCUMENT_REJECTED = "message.document.rejected";
	public static final String CODE_MESSAGE_ERROR_UPDATE_DOCUMENT = "message.error.update.document";
	
	public static final String BASE_PATH = "/document";
	public static final String PATH_LIST = "/list";
	public static final String PATH_SHOW = "/show";
	public static final String PATH_UPDATE = "/update";
	
	public static final String METHOD_ACCEPT = "accept";
	public static final String METHOD_REJECT = "reject";
	public static final String METHOD_ASSIGN = "assign";
	
	public static final String BASE_URL_TEMPLATE = "document";
	
	@Autowired
	private DocumentService documentService;
	@Autowired
	private UserService userService;

	@ModelAttribute("updateDocument")
	public Document populateUpdateDocument() {
		return new Document();
	}

	@ModelAttribute("users")
	public List<User> populateUsers() {
		return userService.findAll();
	}

	@ModelAttribute("actionUpdate")
	public String populateActionUpdate() {
		return BASE_PATH + PATH_UPDATE;
	}
	@ModelAttribute("methodAccept")
	public String populateMethodAccept() {
		return METHOD_ACCEPT;
	}
	@ModelAttribute("methodReject")
	public String populateMethodReject() {
		return METHOD_REJECT;
	}
	@ModelAttribute("methodAssign")
	public String populateMethodAssign() {
		return METHOD_ASSIGN;
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
	
	@RequestMapping(value = PATH_UPDATE, params = { METHOD_ASSIGN }, method=RequestMethod.POST)
	public String assignDocument(final DocumentAssignment assignment, final Model model, final BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return BASE_URL_TEMPLATE + "/show";
		}
		LOG.debug("Assign document '" + assignment.getDocument().getId() + "' to user '" + assignment.getAssignee().getName() + "'");
		//documentService.assignToUser(assignment.getDocument(), assignment.getAssignee());
		return "redirect:" + BASE_PATH + PATH_LIST;
	}

	@RequestMapping(value = PATH_UPDATE, params = { METHOD_ACCEPT }, method=RequestMethod.POST)
	public String acceptDocument(final Document document, final Model model, final RedirectAttributes redirectAttrs) {
		if (document == null || document.getId() == null) {
			redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
			redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_ERROR_UPDATE_DOCUMENT);
		} else {
			final Document result = documentService.acceptDocument(document.getId());
			if (result == null) {
				redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
				redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_ERROR_UPDATE_DOCUMENT);
			} else {
				redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_SUCCESS);
				redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_DOCUMENT_ACCEPTED);
			}
		}
		return "redirect:" + BASE_PATH + PATH_LIST;
	}

	@RequestMapping(value = PATH_UPDATE, params = { METHOD_REJECT }, method=RequestMethod.POST)
	public String rejectDocument(final Document document, final Model model, final RedirectAttributes redirectAttrs) {
		if (document == null || document.getId() == null) {
			redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
			redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_ERROR_UPDATE_DOCUMENT);
		} else {
			final Document result = documentService.rejectDocument(document.getId());
			if (result == null) {
				redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
				redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_ERROR_UPDATE_DOCUMENT);
			} else {
				redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_SUCCESS);
				redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_DOCUMENT_REJECTED);
			}
		}
		return "redirect:" + BASE_PATH + PATH_LIST;
	}

}
