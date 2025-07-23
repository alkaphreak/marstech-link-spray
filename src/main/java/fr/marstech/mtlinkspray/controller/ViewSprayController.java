package fr.marstech.mtlinkspray.controller;

import fr.marstech.mtlinkspray.enums.ViewNameEnum;
import fr.marstech.mtlinkspray.service.SprayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import static fr.marstech.mtlinkspray.enums.ViewNameEnum.SPRAY;
import static fr.marstech.mtlinkspray.utils.NetworkUtils.getHeadersAsMap;

@Controller
@Log
public class ViewSprayController implements ThymeleafViewControllerInterface {

    private final ViewNameEnum viewNameEnum = SPRAY;

    @GetMapping("/spray")
    public ModelAndView getHome(
            HttpServletRequest httpServletRequest
    ) {
        return getModelAndView()
                .addObject("headers", getHeadersAsMap(httpServletRequest));
    }

    @PostMapping("/spray")
    public ModelAndView getLink(
            @RequestParam String inputLinkList,
            HttpServletRequest httpServletRequest
    ) {
        List<String> linkList = SprayService.getLinkList(inputLinkList);
        return getModelAndView()
                .addObject("linkList", linkList)
                .addObject("linkListText", SprayService.getLinkListText(linkList))
                .addObject("linkSpray", SprayService.getLinkSpray(httpServletRequest, linkList));
    }

    @GetMapping("/spray/open")
    public ModelAndView getSpray(
            @RequestParam(name = "spray") List<String> sprayStringList
    ) {
        return getModelAndView()
                .addObject("linkListText", SprayService.getLinkListText(sprayStringList))
                .addObject("spray", sprayStringList);
    }

    @Override
    public ModelAndView getModelAndView() {
        return getModelAndView(viewNameEnum);
    }
}
