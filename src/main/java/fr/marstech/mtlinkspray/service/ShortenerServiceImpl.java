package fr.marstech.mtlinkspray.service;

import fr.marstech.mtlinkspray.entity.LinkItem;
import fr.marstech.mtlinkspray.entity.LinkItemTarget;
import fr.marstech.mtlinkspray.repository.LinkItemRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;

import static fr.marstech.mtlinkspray.utils.NetworkUtils.isValidUrl;

@Log
@Service
public class ShortenerServiceImpl implements ShortenerService {

    final RandomIdGeneratorService randomIdGeneratorService;

    final LinkItemRepository linkItemRepository;

    public ShortenerServiceImpl(
            LinkItemRepository linkItemRepository,
            RandomIdGeneratorService randomIdGeneratorService
    ) {
        this.linkItemRepository = linkItemRepository;
        this.randomIdGeneratorService = randomIdGeneratorService;
    }

    @Override
    public String shorten(String url, HttpServletRequest httpServletRequest) {
        try {
            if (isValidUrl(url)) {
                LinkItem linkItem = new LinkItem().setTarget(new LinkItemTarget().setTargetUrl(url)).setId(getFreeUniqueId());
                LinkItem savedLinkItem = linkItemRepository.save(linkItem);
                return ShortenerService.getShortenedLink(httpServletRequest, savedLinkItem.getId());
            } else {
                throw new MalformedURLException("Invalid URL: %s".formatted(url));
            }
        } catch (MalformedURLException e) {
            log.severe("Error while shortening URL: %s".formatted(url));
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getTarget(String uid, HttpServletRequest httpServletRequest) throws ChangeSetPersister.NotFoundException {
        LinkItem linkItem = linkItemRepository.findById(uid).orElseThrow(ChangeSetPersister.NotFoundException::new);

        // TODO add target management

        return linkItem.getTarget().getTargetUrl();
    }

    private String getFreeUniqueId() {
        return randomIdGeneratorService.getGeneratedFreeId();
    }
}
