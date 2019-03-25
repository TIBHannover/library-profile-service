package eu.tib.profileservice.controller;

import static eu.tib.profileservice.controller.HomeController.ATTRIBUTE_INFO_MESSAGE;
import static eu.tib.profileservice.controller.HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE;
import static eu.tib.profileservice.controller.HomeController.INFO_MESSAGE_TYPE_ERROR;
import static eu.tib.profileservice.controller.HomeController.INFO_MESSAGE_TYPE_SUCCESS;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.User;
import eu.tib.profileservice.service.DocumentService;
import eu.tib.profileservice.service.UserService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.data.web.SortDefault.SortDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value = DocumentController.BASE_PATH)
public class DocumentController {

  private static final Logger LOG = LoggerFactory.getLogger(DocumentController.class);

  public static final String CODE_MESSAGE_DOCUMENT_ACCEPTED = "message.document.accepted";
  public static final String CODE_MESSAGE_DOCUMENT_REJECTED = "message.document.rejected";
  public static final String CODE_MESSAGE_DOCUMENT_ASSIGNED = "message.document.assigned";
  public static final String CODE_MESSAGE_ERROR_UPDATE_DOCUMENT = "message.error.update.document";

  public static final String BASE_PATH = "/document";
  public static final String PATH_LIST = "/list";
  public static final String PATH_MY = "/my";
  public static final String PATH_UPDATE = "/update";

  public static final String METHOD_ACCEPT = "accept";
  public static final String METHOD_REJECT = "reject";
  public static final String METHOD_ASSIGN = "assign";

  public static final String BASE_URL_TEMPLATE = "document";
  public static final String TEMPLATE_LIST = "/list";

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
    return "redirect:" + BASE_PATH + PATH_MY;
  }

  public static String buildSearchQuery(final Document search) {
    final StringBuilder sb = new StringBuilder();
    if (search != null) {
      if (search.getAssignee() != null && search.getAssignee().getId() != null) {
        sb.append("assignee=").append(search.getAssignee().getId());
      }
      if (search.getId() != null) {
        if (sb.length() > 0) {
          sb.append("&");
        }
        sb.append("id=").append(search.getId());
      }
      if (search.getStatus() != null) {
        if (sb.length() > 0) {
          sb.append("&");
        }
        sb.append("status=").append(search.getStatus().toString());
      }
    }
    return sb.toString();
  }

  @GetMapping(PATH_LIST)
  public String list(final Document search, final Model model,
      @SortDefaults({@SortDefault(sort = "creationDateUtc",
          direction = Sort.Direction.DESC)}) final Pageable pageable) {
    LOG.debug("pageable: {}", pageable);
    LOG.debug("search: {}", search);
    LOG.debug("sort: {}", pageable.getSort());
    final Page<Document> documents = documentService.findAllByExample(search, pageable);
    model.addAttribute("documents", documents.getContent());
    model.addAttribute("page", documents);
    model.addAttribute("search", search);
    return BASE_URL_TEMPLATE + TEMPLATE_LIST;
  }

  @GetMapping(PATH_MY)
  public String myDocuments(final Model model, final Pageable pageable) {
    final User user = getCurrentUser();
    String searchQuery = "";
    if (user != null) {
      Document exampleSearch = new Document();
      exampleSearch.setAssignee(user);
      exampleSearch.setStatus(Document.Status.IN_PROGRESS);
      searchQuery = buildSearchQuery(exampleSearch);
    }
    return "redirect:" + BASE_PATH + PATH_LIST + (searchQuery.length() > 0 ? ("?" + searchQuery)
        : "");
  }

  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
      return userService.findByName(authentication.getName());
    }
    return null;
  }

  private String getRedirectUri(final String sourceUri, final String sourceQuery) {
    final StringBuilder sb = new StringBuilder();
    if (sourceUri != null && sourceUri.length() > 0) {
      sb.append(sourceUri);
    } else {
      sb.append("/");
    }
    if (sourceQuery != null && sourceQuery.length() > 0) {
      sb.append("?").append(sourceQuery);
    }
    return sb.toString();
  }

  @RequestMapping(value = PATH_UPDATE, params = {METHOD_ASSIGN}, method = RequestMethod.POST)
  public String assignDocument(final Document document, final Model model,
      final RedirectAttributes redirectAttrs, final String sourceUri, final String sourceQuery) {
    String redirectUri = getRedirectUri(sourceUri, sourceQuery);
    LOG.debug("redirectUri: {}", redirectUri);
    if (document == null || document.getId() == null) {
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_ERROR_UPDATE_DOCUMENT);
      return "redirect:" + redirectUri;
    }
    if (document.getAssignee() == null || document.getAssignee().getId() == null) {
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_ERROR_UPDATE_DOCUMENT);
      return "redirect:" + redirectUri;
    }
    LOG.debug("Assign document '{}' to user '{}'", document.getId(), document.getAssignee()
        .getName());
    final Document result = documentService.assignToUser(document, document.getAssignee());
    if (result == null) {
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_ERROR_UPDATE_DOCUMENT);
    } else {
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_SUCCESS);
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_DOCUMENT_ASSIGNED);
      redirectAttrs.addFlashAttribute(
          eu.tib.profileservice.controller.HomeController.ATTRIBUTE_INFO_MESSAGE_PARAMETER,
          result.getAssignee().getName());
    }
    return "redirect:" + redirectUri;
  }

  @RequestMapping(value = PATH_UPDATE, params = {METHOD_ACCEPT}, method = RequestMethod.POST)
  public String acceptDocument(final Document document, final Model model,
      final RedirectAttributes redirectAttrs, final String sourceUri, final String sourceQuery) {
    String redirectUri = getRedirectUri(sourceUri, sourceQuery);
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
    return "redirect:" + redirectUri;
  }

  @RequestMapping(value = PATH_UPDATE, params = {METHOD_REJECT}, method = RequestMethod.POST)
  public String rejectDocument(final Document document, final Model model,
      final RedirectAttributes redirectAttrs, final String sourceUri, final String sourceQuery) {
    String redirectUri = getRedirectUri(sourceUri, sourceQuery);
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
    return "redirect:" + redirectUri;
  }

}
