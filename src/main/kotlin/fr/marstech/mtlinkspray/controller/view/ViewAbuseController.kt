package fr.marstech.mtlinkspray.controller.view;

import fr.marstech.mtlinkspray.enums.ViewNameEnum;
import fr.marstech.mtlinkspray.service.ReportAbuseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import static fr.marstech.mtlinkspray.enums.ViewNameEnum.ABUSE;
import static fr.marstech.mtlinkspray.utils.NetworkUtils.getHeadersAsMap;

@Controller
public class ViewAbuseController implements ThymeleafViewControllerInterface {

    private static final ViewNameEnum viewNameEnum = ABUSE;

    private final ReportAbuseService reportAbuseService;

    public ViewAbuseController(ReportAbuseService reportAbuseService) {
        this.reportAbuseService = reportAbuseService;
    }

    @GetMapping("/abuse")
    public ModelAndView getView(HttpServletRequest httpServletRequest) {
        return getModelAndView().addObject("headers", getHeadersAsMap(httpServletRequest));
    }

    @PostMapping("/abuse")
    public ModelAndView postAbuseForm(
            @RequestParam String inputAbuseDecsription,
            HttpServletRequest httpServletRequest) {
        reportAbuseService.reportAbuse(inputAbuseDecsription, httpServletRequest);

        return getModelAndView().addObject("headers", getHeadersAsMap(httpServletRequest));
    }

    @Override
    public ModelAndView getModelAndView() {
        return getModelAndView(viewNameEnum);
    }
}
