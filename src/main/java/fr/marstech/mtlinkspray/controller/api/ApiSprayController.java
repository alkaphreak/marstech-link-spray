package fr.marstech.mtlinkspray.controller.api;

import fr.marstech.mtlinkspray.service.SprayService;
import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@Log
public class ApiSprayController {

    @GetMapping("/api/spray")
    public String getSprayLink(
            HttpServletRequest httpServletRequest,
            @RequestParam List<String> inputLinkList
    ) {
        return SprayService.getLinkSpray(httpServletRequest, inputLinkList);
    }
}
