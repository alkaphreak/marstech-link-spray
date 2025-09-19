package fr.marstech.mtlinkspray.controller;

import fr.marstech.mtlinkspray.controller.api.ApiShortenerController;
import fr.marstech.mtlinkspray.enums.ViewNameEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import static fr.marstech.mtlinkspray.enums.ViewNameEnum.SHORTENER;

@Validated
@Controller
public class ViewShortenerController implements ThymeleafViewControllerInterface {

  private static final ViewNameEnum viewNameEnum = SHORTENER;

  final ApiShortenerController apiShortenerController;

  public ViewShortenerController(ApiShortenerController apiShortenerController) {
    this.apiShortenerController = apiShortenerController;
  }

  @Override
  public ModelAndView getModelAndView() {
    return getModelAndView(viewNameEnum);
  }

  @GetMapping("/shortener")
  public ModelAndView getView() {
    return getModelAndView();
  }

  @PostMapping("/shortener")
  public ModelAndView postView(
      @RequestParam(name = "input-link") @NotBlank String inputLink,
      HttpServletRequest httpServletRequest) {
    return getModelAndView()
        .addObject("inputLink", inputLink)
        .addObject(
            "shortenedLink", apiShortenerController.getShort(inputLink, httpServletRequest));
  }
}
