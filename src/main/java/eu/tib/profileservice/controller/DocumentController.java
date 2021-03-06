package eu.tib.profileservice.controller;

import static eu.tib.profileservice.controller.HomeController.ATTRIBUTE_INFO_MESSAGE;
import static eu.tib.profileservice.controller.HomeController.ATTRIBUTE_INFO_MESSAGE_TYPE;
import static eu.tib.profileservice.controller.HomeController.INFO_MESSAGE_TYPE_ERROR;
import static eu.tib.profileservice.controller.HomeController.INFO_MESSAGE_TYPE_SUCCESS;

import eu.tib.profileservice.domain.Document;
import eu.tib.profileservice.domain.Document.Status;
import eu.tib.profileservice.domain.DocumentSearch;
import eu.tib.profileservice.domain.User;
import eu.tib.profileservice.service.DocumentService;
import eu.tib.profileservice.service.UserService;
import eu.tib.profileservice.util.FileExportProcessor;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.data.web.SortDefault.SortDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value = DocumentController.BASE_PATH)
public class DocumentController {

  private static final Logger LOG = LoggerFactory.getLogger(DocumentController.class);

  public static final String CODE_MESSAGE_DOCUMENT_ACCEPTED = "message.document.accepted";
  public static final String CODE_MESSAGE_DOCUMENT_REJECTED = "message.document.rejected";
  public static final String CODE_MESSAGE_DOCUMENT_ASSIGNED = "message.document.assigned";
  public static final String CODE_MESSAGE_DOCUMENT_PENDING = "message.document.pending";
  public static final String CODE_MESSAGE_ERROR_UPDATE_DOCUMENT = "message.error.update.document";
  private static final String CODE_MESSAGE_DOCUMENTS_EXPORTED = "message.documents.exported";
  private static final String CODE_MESSAGE_ERROR_EXPORT_DOCUMENTS =
      "message.error.export.documents";

  public static final String BASE_PATH = "/document";
  public static final String PATH_LIST = "/list";
  public static final String PATH_MY = "/my";
  public static final String PATH_UPDATE = "/update";
  /** path export. */
  public static final String PATH_EXPORT = "/export";

  public static final String METHOD_ACCEPT = "accept";
  public static final String METHOD_REJECT = "reject";
  public static final String METHOD_ASSIGN = "assign";
  public static final String METHOD_PENDING = "pending";
  /** method export. */
  public static final String METHOD_EXPORT = "export";
  /** method download. */
  public static final String METHOD_DOWNLOAD = "download";

  public static final String BASE_URL_TEMPLATE = "document";
  public static final String TEMPLATE_LIST = "/list";
  private static final String TEMPLATE_EXPORT = "/export";

  @Autowired
  private DocumentService documentService;
  @Autowired
  private UserService userService;
  @Autowired
  private FileExportProcessor fileExportProcessor;

  @ModelAttribute("updateDocument")
  public Document populateUpdateDocument() {
    return new Document();
  }

  @ModelAttribute("users")
  public List<User> populateUsers() {
    return userService.findAll();
  }

  @ModelAttribute("availableDocumentStatus")
  public Status[] populateStatus() {
    return Status.values();
  }

  @ModelAttribute("actionUpdate")
  public String populateActionUpdate() {
    return BASE_PATH + PATH_UPDATE;
  }

  @ModelAttribute("actionList")
  public String populateActionList() {
    return BASE_PATH + PATH_LIST;
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

  @ModelAttribute("methodPending")
  public String populateMethodPending() {
    return METHOD_PENDING;
  }

  @ModelAttribute("availableSizes")
  public int[] populateAvailableSizes() {
    return new int[] {20, 50, 100};
  }

  @GetMapping("**")
  public String index() {
    return "redirect:" + BASE_PATH + PATH_MY;
  }

  /**
   * Build the query string for the given {@link DocumentSearch}.
   * @param search search
   * @return query string
   */
  public static String buildSearchQuery(final DocumentSearch search, final Pageable pageable) {
    final StringBuilder sb = new StringBuilder();
    if (search != null) {
      if (search.getAssignee() != null && search.getAssignee().getId() != null) {
        sb.append("assignee=").append(search.getAssignee().getId());
      }
      if (search.getStatus() != null) {
        if (sb.length() > 0) {
          sb.append("&");
        }
        sb.append("status=").append(search.getStatus().toString());
      }
      if (search.getCreationDateFrom() != null) {
        if (sb.length() > 0) {
          sb.append("&");
        }
        sb.append("creationDateFrom=").append(search.getCreationDateFrom().toString());
      }
      if (search.getCreationDateTo() != null) {
        if (sb.length() > 0) {
          sb.append("&");
        }
        sb.append("creationDateTo=").append(search.getCreationDateTo().toString());
      }
    }
    if (pageable != null && pageable.isPaged()) {
      if (sb.length() > 0) {
        sb.append("&");
      }
      sb.append("size=").append(pageable.getPageSize());
    }
    return sb.toString();
  }

  /**
   * List all {@link Document}s matching the given search.
   * @param search search
   * @param model model
   * @param pageable pageable
   * @return template
   */
  @GetMapping(PATH_LIST)
  public String list(final DocumentSearch search, final Model model,
      @SortDefaults({@SortDefault(sort = "creationDateUtc",
          direction = Sort.Direction.DESC)}) final Pageable pageable) {
    //    LOG.debug("pageable: {}", pageable);
    //    LOG.debug("search: {}", search);
    //    LOG.debug("sort: {}", pageable.getSort());
    final Page<Document> documents = documentService.findAllByDocumentSearch(search, pageable);
    model.addAttribute("documents", documents.getContent());
    model.addAttribute("updateDocuments", new DocumentListDto(documents.getContent()));
    model.addAttribute("page", documents);
    model.addAttribute("search", search);
    return BASE_URL_TEMPLATE + TEMPLATE_LIST;
  }

  /**
   * List all {@link Document}s assigned to the current user.
   * @param model model
   * @param pageable pageable
   * @return template
   */
  @GetMapping(PATH_MY)
  public String myDocuments(final Model model, final Pageable pageable) {
    final User user = getCurrentUser();
    String searchQuery = "";
    if (user != null) {
      DocumentSearch exampleSearch = new DocumentSearch();
      exampleSearch.setAssignee(user);
      exampleSearch.setStatus(Document.Status.IN_PROGRESS);

      OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
      LocalDate now = utc.toLocalDate();
      exampleSearch.setCreationDateFrom(utc.minusDays(7).toLocalDate());
      exampleSearch.setCreationDateTo(now);

      searchQuery = buildSearchQuery(exampleSearch, pageable);
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

  /**
   * Change the assignment of the given {@link Document}s.
   * The documents will be assigned to the user identified by assigneeId.
   * @param documents documents to assign
   * @param sourceUri source uri of the request
   * @param sourceQuery query of the request
   * @param assigneeId id of the assignee user
   * @param redirectAttrs redirectAttrs
   * @return
   */
  @RequestMapping(value = PATH_UPDATE, params = {METHOD_ASSIGN}, method = RequestMethod.POST)
  public String assignDocuments(final DocumentListDto documents, final String sourceUri,
      final String sourceQuery, @RequestParam(name = "assigneeId") final Long assigneeId,
      final RedirectAttributes redirectAttrs) {
    String redirectUri = getRedirectUri(sourceUri, sourceQuery);
    //    LOG.debug("redirectUri: {}", redirectUri);
    List<Document> selectedDocuments = getSelectedDocuments(documents);
    if (selectedDocuments.isEmpty()) {
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_ERROR_UPDATE_DOCUMENT);
      return "redirect:" + redirectUri;
    }
    boolean errorOccurred = false;
    String assignee = "";
    for (Document document : selectedDocuments) {
      LOG.debug("Assign document '{}' to user '{}'", document.getId(), assigneeId);
      final Document result = documentService.assignToUser(document, assigneeId);
      if (result == null) {
        errorOccurred = true;
      } else {
        assignee = result.getAssignee().getName();
      }
    }
    if (errorOccurred) {
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_ERROR_UPDATE_DOCUMENT);
    } else {
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_SUCCESS);
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_DOCUMENT_ASSIGNED);
      redirectAttrs.addFlashAttribute(
          eu.tib.profileservice.controller.HomeController.ATTRIBUTE_INFO_MESSAGE_PARAMETER,
          assignee);
    }
    return "redirect:" + redirectUri;
  }

  private List<Document> getSelectedDocuments(final DocumentListDto documents) {
    List<Document> result = new ArrayList<>();
    if (documents.getDocuments() != null && documents.getSelected() != null) {
      for (int i = 0; i < documents.getDocuments().size(); i++) {
        Document document = documents.getDocuments().get(i);
        Boolean selected = documents.getSelected().get(i);
        //        LOG.debug("document {} {}", document.getId(), selected);
        if (Boolean.TRUE.equals(selected) && document.getId() != null) {
          result.add(document);
        }
      }
    }
    return result;
  }

  /**
   * Accept the given {@link Document}s. (set status to {@link Status#ACCEPTED})
   * @param documents documents to accept
   * @param model model
   * @param redirectAttrs redirectAttrs
   * @param sourceUri source uri of the request
   * @param sourceQuery query of the request
   * @return template
   */
  @RequestMapping(value = PATH_UPDATE, params = {METHOD_ACCEPT}, method = RequestMethod.POST)
  public String acceptDocuments(final DocumentListDto documents, final Model model,
      final RedirectAttributes redirectAttrs, final String sourceUri, final String sourceQuery) {
    String redirectUri = getRedirectUri(sourceUri, sourceQuery);
    List<Document> selectedDocuments = getSelectedDocuments(documents);
    if (selectedDocuments.isEmpty()) {
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_ERROR_UPDATE_DOCUMENT);
    } else {
      boolean errorOccurred = false;
      for (Document document : selectedDocuments) {
        LOG.debug("accept doc {}", document.getId());
        final Document result = documentService.acceptDocument(document.getId());
        if (result == null) {
          errorOccurred = true;
        }
      }
      if (errorOccurred) {
        redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
        redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_ERROR_UPDATE_DOCUMENT);
      } else {
        redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_SUCCESS);
        redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_DOCUMENT_ACCEPTED);
      }
    }
    return "redirect:" + redirectUri;
  }

  /**
   * Reject the given {@link Document}s. (set status to {@link Status#REJECTED})
   * @param documents documents to reject
   * @param model model
   * @param redirectAttrs redirectAttrs
   * @param sourceUri source uri of the request
   * @param sourceQuery query of the request
   * @return template
   */
  @RequestMapping(value = PATH_UPDATE, params = {METHOD_REJECT}, method = RequestMethod.POST)
  public String rejectDocument(final DocumentListDto documents, final Model model,
      final RedirectAttributes redirectAttrs, final String sourceUri, final String sourceQuery) {
    String redirectUri = getRedirectUri(sourceUri, sourceQuery);
    List<Document> selectedDocuments = getSelectedDocuments(documents);
    if (selectedDocuments.isEmpty()) {
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_ERROR_UPDATE_DOCUMENT);
    } else {
      boolean errorOccurred = false;
      for (Document document : selectedDocuments) {
        LOG.debug("reject doc {}", document.getId());
        final Document result = documentService.rejectDocument(document.getId());
        if (result == null) {
          errorOccurred = true;
        }
      }
      if (errorOccurred) {
        redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
        redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_ERROR_UPDATE_DOCUMENT);
      } else {
        redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_SUCCESS);
        redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_DOCUMENT_REJECTED);
      }
    }
    return "redirect:" + redirectUri;
  }

  /**
   * Set the {@link Status} of the given {@link Document}s to {@link Status#PENDING}
   * and adjust the expiry date.
   * @param documents documents to update
   * @param expiryDateUtc expiryDateUtc new expiry date
   * @param model model
   * @param redirectAttrs redirectAttrs
   * @param sourceUri source uri of the request
   * @param sourceQuery query of the request
   * @return template
   */
  @RequestMapping(value = PATH_UPDATE, params = {METHOD_PENDING}, method = RequestMethod.POST)
  public String setDocumentToPending(final DocumentListDto documents,
      @RequestParam(name = "newExpiryDate", required = false) final String expiryDateUtc,
      final Model model, final RedirectAttributes redirectAttrs, final String sourceUri,
      final String sourceQuery) {
    String redirectUri = getRedirectUri(sourceUri, sourceQuery);
    List<Document> selectedDocuments = getSelectedDocuments(documents);
    if (selectedDocuments.isEmpty()) {
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_ERROR_UPDATE_DOCUMENT);
    } else {
      boolean errorOccurred = false;
      for (Document document : selectedDocuments) {
        Document origDocument = documentService.findById(document.getId());
        if (origDocument == null) {
          errorOccurred = true;
        } else {
          origDocument.setStatus(Status.PENDING);
          if (expiryDateUtc != null) {
            origDocument.setExpiryDateUtc(LocalDate.parse(expiryDateUtc).atStartOfDay());
          }
          documentService.saveDocument(origDocument);
        }
      }
      if (errorOccurred) {
        redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
        redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_ERROR_UPDATE_DOCUMENT);
      } else {
        redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_SUCCESS);
        redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_DOCUMENT_PENDING);
      }
    }
    return "redirect:" + redirectUri;
  }

  /**
   * The export template.
   * @param model model
   * @return template
   */
  @RequestMapping(path = PATH_EXPORT, method = RequestMethod.GET)
  public String export(final Model model) {
    Document example = new Document();
    example.setStatus(Status.ACCEPTED);
    model.addAttribute("countReadyToExport", documentService.countByExample(example));
    model.addAttribute("files", fileExportProcessor.listExportFiles());
    return BASE_URL_TEMPLATE + TEMPLATE_EXPORT;
  }

  /**
   * Process the document-export.
   * @return template
   */
  @RequestMapping(path = PATH_EXPORT, params = {METHOD_EXPORT}, method = RequestMethod.GET)
  public String processExport(final RedirectAttributes redirectAttrs) {
    boolean success = documentService.export();
    if (success) {
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_SUCCESS);
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_DOCUMENTS_EXPORTED);
    } else {
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE_TYPE, INFO_MESSAGE_TYPE_ERROR);
      redirectAttrs.addFlashAttribute(ATTRIBUTE_INFO_MESSAGE, CODE_MESSAGE_ERROR_EXPORT_DOCUMENTS);
    }
    return "redirect:" + BASE_PATH + PATH_EXPORT;
  }

  /**
   * Download the export file.
   * @param file filename
   * @return download
   */
  @RequestMapping(path = PATH_EXPORT, params = {METHOD_DOWNLOAD}, method = RequestMethod.GET)
  public ResponseEntity<Resource> download(@RequestParam("file") final String file) {
    byte[] bytes = fileExportProcessor.getBytesOfExportFile(file);
    if (bytes != null) {
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file)
          .contentLength(bytes.length)
          .contentType(MediaType.parseMediaType("application/octet-stream"))
          .body(new ByteArrayResource(bytes));
    }
    return ResponseEntity.noContent().build();
  }

}
