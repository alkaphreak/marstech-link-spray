package fr.marstech.mtlinkspray.controller;

import fr.marstech.mtlinkspray.enums.ViewNameEnum;
import lombok.extern.java.Log;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static fr.marstech.mtlinkspray.service.SprayService.*;
import static fr.marstech.mtlinkspray.utils.NetworkUtils.*;

@Controller
@Log
public class ViewSprayController implements ThymeleafViewControllerInterface {

    @GetMapping("/")
    public ModelAndView getHome(
            HttpServletRequest httpServletRequest
    ) {
        return getModelAndView()
                .addObject("headers", getHeadersAsMap(httpServletRequest));
    }

    @PostMapping("/")
    public ModelAndView getLink(
            @RequestParam String inputLinkList,
            HttpServletRequest httpServletRequest
    ) {
        List<String> linkList = getLinkList(inputLinkList);
        return getModelAndView()
                .addObject("linkList", linkList)
                .addObject("linkListText", getLinkListText(linkList))
                .addObject("linkSpray", getLinkSpray(httpServletRequest, linkList));
    }

    @GetMapping("/open")
    public ModelAndView getSpray(
            @RequestParam List<String> sprayStringList
    ) {
        return getModelAndView()
                .addObject("linkListText", getLinkListText(sprayStringList))
                .addObject("spray", sprayStringList);
    }

    @Override
    public ModelAndView getModelAndView() {
        return new ModelAndView("index")
                .addObject("viewName", ViewNameEnum.SPRAY);
    }
}
