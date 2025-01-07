package fr.marstech.mtlinkspray.controller.api;

import fr.marstech.mtlinkspray.service.ShortenerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;

@RestController
public class ApiUrlShortenerController {

    final ShortenerService shortenerService;

    public ApiUrlShortenerController(ShortenerService shortenerService) {
        this.shortenerService = shortenerService;
    }

    @GetMapping("/api/url-shortener/shorten")
    public URL getShort(
            @RequestParam(name = "url") String inputUrl
    ) {
        return shortenerService.shorten(inputUrl);
    }
}
