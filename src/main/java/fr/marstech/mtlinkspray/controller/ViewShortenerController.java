package fr.marstech.mtlinkspray.controller;

import fr.marstech.mtlinkspray.controller.api.ApiUrlShortenerController;
import fr.marstech.mtlinkspray.enums.ViewNameEnum;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.net.MalformedURLException;

import static fr.marstech.mtlinkspray.enums.ViewNameEnum.SHORTENER;

@Controller
public class ViewShortenerController implements ThymeleafViewControllerInterface {

    private final ViewNameEnum viewNameEnum = SHORTENER;

    final ApiUrlShortenerController apiUrlShortenerController;

    public ViewShortenerController(ApiUrlShortenerController apiUrlShortenerController) {
        this.apiUrlShortenerController = apiUrlShortenerController;
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
            @RequestParam(name = "input-link") String inputLink,
            HttpServletRequest httpServletRequest
    ) throws MalformedURLException {
        return getModelAndView()
                .addObject("inputLink", inputLink)
                .addObject("shortenedLink", apiUrlShortenerController.getShort(inputLink, httpServletRequest));
    }
}