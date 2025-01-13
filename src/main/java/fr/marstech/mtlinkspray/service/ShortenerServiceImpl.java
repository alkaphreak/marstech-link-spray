package fr.marstech.mtlinkspray.service;

import fr.marstech.mtlinkspray.entity.LinkItem;
import fr.marstech.mtlinkspray.entity.LinkItemTarget;
import fr.marstech.mtlinkspray.repository.LinkItemRepository;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;

import static fr.marstech.mtlinkspray.utils.NetworkUtils.isValidUrl;

@Log
@Service
public class ShortenerServiceImpl implements ShortenerService {

    final RandomIdGeneratorService randomIdGeneratorService;

    final LinkItemRepository linkItemRepository;

    public ShortenerServiceImpl(LinkItemRepository linkItemRepository, RandomIdGeneratorService randomIdGeneratorService) {
        this.linkItemRepository = linkItemRepository;
        this.randomIdGeneratorService = randomIdGeneratorService;
    }

    @Override
    public URL shorten(String url) {
        try {
            if (isValidUrl(url)) {
                LinkItem linkItem = new LinkItem().setTarget(new LinkItemTarget().setTargetUrl(url)).setId(getFreeUniqueId());
                LinkItem savedLinkItem = linkItemRepository.save(linkItem);
                return new URL("http://localhost:8080/" + savedLinkItem.getId());
            } else {
                throw new MalformedURLException("Invalid URL: " + url);
            }
        } catch (MalformedURLException e) {
            log.severe("Error while shortening URL: " + url);
            throw new RuntimeException(e);
        }
    }

    private String getFreeUniqueId() {
        return randomIdGeneratorService.getGeneratedFreeId();
    }
}
