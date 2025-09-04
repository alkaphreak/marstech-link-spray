package fr.marstech.mtlinkspray.controller.api;

import fr.marstech.mtlinkspray.service.ShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import jakarta.validation.constraints.NotBlank;

import static java.text.MessageFormat.format;

@Validated
@RestController
public class ApiUrlShortenerController {

  final ShortenerService shortenerService;

  public ApiUrlShortenerController(ShortenerService shortenerService) {
    this.shortenerService = shortenerService;
  }

  /**
   * Generate a short link
   *
   * @param inputUrl the url input
   * @param httpServletRequest th httpServletRequest
   * @return the url shortened
   */
  @GetMapping("/api/url-shortener/shorten")
  public String getShort(
      @RequestParam(name = "url") @NotBlank String inputUrl,
      HttpServletRequest httpServletRequest) {
    return shortenerService.shorten(inputUrl, httpServletRequest);
  }

  /**
   * Forward a short link to the real link
   *
   * @param httpServletRequest the httpServletRequest
   * @param shortUrlUid the short code of the url
   * @param model the model of the view
   * @return a {@link ModelAndView} with forward to the target url
   * @throws ChangeSetPersister.NotFoundException in case the db did not find
   */
  @GetMapping("/{shortUrlUid}")
  public ModelAndView getTarget(
      HttpServletRequest httpServletRequest,
      @PathVariable(name = "shortUrlUid") String shortUrlUid,
      ModelMap model)
      throws ChangeSetPersister.NotFoundException {
    model.addAttribute("attribute", "redirectWithRedirectPrefix");
    return new ModelAndView(
        format("redirect:{0}", shortenerService.getTarget(shortUrlUid, httpServletRequest)), model);
  }
}
