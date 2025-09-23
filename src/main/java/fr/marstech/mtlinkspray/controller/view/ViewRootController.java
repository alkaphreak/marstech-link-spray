package fr.marstech.mtlinkspray.controller.view;

import fr.marstech.mtlinkspray.enums.ViewNameEnum;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import static fr.marstech.mtlinkspray.enums.ViewNameEnum.HOME;

@Controller
public class ViewRootController implements ThymeleafViewControllerInterface {

    private static final ViewNameEnum viewNameEnum = HOME;

    @Override
    public ModelAndView getModelAndView() {
        return getModelAndView(viewNameEnum);
    }

    @GetMapping("/index")
    public ModelAndView getIndex() {
        return getModelAndView();
    }

    @GetMapping("/")
    public ModelAndView getHome() {
        return getModelAndView();
    }
}
