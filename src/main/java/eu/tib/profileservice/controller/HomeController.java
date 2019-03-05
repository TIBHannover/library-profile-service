package eu.tib.profileservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

  /** Attribute name of the info message used in RedirectAttributes.FlashAttribute. **/
  public static final String ATTRIBUTE_INFO_MESSAGE = "infoMessage";
  /** Attribute name of the info message parameter used in RedirectAttributes.FlashAttribute. **/
  public static final String ATTRIBUTE_INFO_MESSAGE_PARAMETER = "infoMessageParameter";
  /** Attribute name of the info message type used in RedirectAttributes.FlashAttribute. **/
  public static final String ATTRIBUTE_INFO_MESSAGE_TYPE = "infoMessageType";
  /** info message type for success message. **/
  public static final String INFO_MESSAGE_TYPE_SUCCESS = "success";
  /** info message type for error message. **/
  public static final String INFO_MESSAGE_TYPE_ERROR = "error";

  @GetMapping("/")
  public String index() {
    return "redirect:" + DocumentController.BASE_PATH;
  }

}
