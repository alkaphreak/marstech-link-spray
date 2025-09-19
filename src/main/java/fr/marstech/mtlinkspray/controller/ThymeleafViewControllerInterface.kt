package fr.marstech.mtlinkspray.controller;

import fr.marstech.mtlinkspray.enums.ViewNameEnum;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import static fr.marstech.mtlinkspray.utils.NetworkUtils.getHeadersAsMap;
import static java.text.MessageFormat.format;

public interface ThymeleafViewControllerInterface {

    ModelAndView getModelAndView();

    default ModelAndView getModelAndView(ViewNameEnum viewNameEnum) {
        return new ModelAndView(viewNameEnum.getViewName()).addObject("viewNameEnum", viewNameEnum);
    }

    default ModelAndView getModelAndView(ViewNameEnum viewNameEnum, HttpServletRequest httpServletRequest) {
        return new ModelAndView(viewNameEnum.getViewName()).addObject("viewNameEnum", viewNameEnum).addObject("headers", getHeadersAsMap(httpServletRequest));
    }

    default ModelAndView getModelAndViewToForward(String forwardUrl) {
        return new ModelAndView(format("forward:{0}", forwardUrl));
    }

    default String getRedirectUrl(String url) {
        return format("redirect:{0}", url);
    }
}
