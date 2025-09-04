package fr.marstech.mtlinkspray.controller;

import fr.marstech.mtlinkspray.enums.ViewNameEnum;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import static fr.marstech.mtlinkspray.enums.ViewNameEnum.ERROR;

@ControllerAdvice
public class GlobalExceptionHandler implements ThymeleafViewControllerInterface {

  private static final ViewNameEnum viewNameEnum = ERROR;

  @ExceptionHandler(Exception.class)
  public ModelAndView handleException(Exception e) {
    return getModelAndView().addObject("errorMessage", e.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ModelAndView handleIllegalArgument(IllegalArgumentException e) {
    return getModelAndView().addObject("errorMessage", e.getMessage());
  }

  @Override
  public ModelAndView getModelAndView() {
    return getModelAndView(viewNameEnum);
  }
}
