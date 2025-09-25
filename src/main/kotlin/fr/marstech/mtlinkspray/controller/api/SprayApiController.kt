package fr.marstech.mtlinkspray.controller.api;

import fr.marstech.mtlinkspray.service.SprayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SprayApiController {

    @GetMapping("/api/spray")
    public String getSprayLink(
            HttpServletRequest httpServletRequest,
            @RequestParam List<String> inputLinkList
    ) {
        return SprayService.getLinkSpray(httpServletRequest, inputLinkList);
    }
}
