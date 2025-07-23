package fr.marstech.mtlinkspray.controller;

import fr.marstech.mtlinkspray.enums.ViewNameEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import static fr.marstech.mtlinkspray.enums.ViewNameEnum.HOME;

@Slf4j
@Controller
public class ViewRootController implements ThymeleafViewControllerInterface {

    private static final ViewNameEnum viewNameEnum = HOME;

    @Override
    public ModelAndView getModelAndView() {
        log.info("getModelAndView:{}", viewNameEnum);
        return getModelAndView(viewNameEnum);
    }

    @GetMapping("/")
    public ModelAndView getHome() {
        log.info("getHome");
        return getModelAndView();
    }
}