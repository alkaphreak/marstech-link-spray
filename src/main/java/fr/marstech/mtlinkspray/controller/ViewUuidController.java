package fr.marstech.mtlinkspray.controller;

import static fr.marstech.mtlinkspray.enums.ViewNameEnum.UUID;

import fr.marstech.mtlinkspray.controller.api.ApiUuidController;
import fr.marstech.mtlinkspray.enums.ViewNameEnum;
import lombok.extern.java.Log;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Log
public class ViewUuidController implements ThymeleafViewControllerInterface {

  private static final ViewNameEnum viewNameEnum = UUID;

  final ApiUuidController apiUuidController;

  public ViewUuidController(ApiUuidController apiUuidController) {
    this.apiUuidController = apiUuidController;
  }

  @GetMapping("/uuid")
  public ModelAndView getHome() {
    return getModelAndView().addObject("uuid", apiUuidController.getUuid());
  }

  @Override
  public ModelAndView getModelAndView() {
    return getModelAndView(viewNameEnum);
  }
}
