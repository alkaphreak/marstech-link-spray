package fr.marstech.mtlinkspray.controller.api;

import fr.marstech.mtlinkspray.service.ShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import static java.text.MessageFormat.format;

@RestController
public class ApiUrlShortenerController {

    final ShortenerService shortenerService;

    public ApiUrlShortenerController(ShortenerService shortenerService) {
        this.shortenerService = shortenerService;
    }

    @GetMapping("/api/url-shortener/shorten")
    public String getShort(
            @RequestParam(name = "url") String inputUrl, HttpServletRequest httpServletRequest
    ) {
        return shortenerService.shorten(inputUrl, httpServletRequest);
    }

    @GetMapping("/{shortUrlUid}")
    public ModelAndView getTarget(HttpServletRequest httpServletRequest, @PathVariable(name = "shortUrlUid") String shortUrlUid, ModelMap model) throws ChangeSetPersister.NotFoundException {
        model.addAttribute("attribute", "redirectWithRedirectPrefix");
        return new ModelAndView(format("redirect:{0}", shortenerService.getTarget(shortUrlUid, httpServletRequest)), model);
    }
}
