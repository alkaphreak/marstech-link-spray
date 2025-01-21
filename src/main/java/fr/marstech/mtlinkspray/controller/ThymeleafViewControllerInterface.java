package fr.marstech.mtlinkspray.controller;

import fr.marstech.mtlinkspray.enums.ViewNameEnum;
import org.springframework.web.servlet.ModelAndView;

public interface ThymeleafViewControllerInterface {

    ModelAndView getModelAndView();

    default ModelAndView getModelAndView(ViewNameEnum viewNameEnum) {
        return new ModelAndView(viewNameEnum.getViewName())
                .addObject("viewNameEnum", viewNameEnum);
    }
}
