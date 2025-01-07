package fr.marstech.mtlinkspray.controller.api;

import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static fr.marstech.mtlinkspray.service.SprayService.getLinkSpray;

@RestController
@Log
public class ApiSprayController {

    @GetMapping("/api/spray")
    public String getSprayLink(
            HttpServletRequest httpServletRequest,
            @RequestParam List<String> inputLinkList
    ) {
        return getLinkSpray(httpServletRequest, inputLinkList);
    }
}
