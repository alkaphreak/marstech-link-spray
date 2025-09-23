package fr.marstech.mtlinkspray.service;

import fr.marstech.mtlinkspray.entity.HistoryItem;
import fr.marstech.mtlinkspray.entity.LinkItem;
import fr.marstech.mtlinkspray.entity.LinkItemTarget;
import fr.marstech.mtlinkspray.exception.UrlShorteningException;
import fr.marstech.mtlinkspray.repository.LinkItemRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static fr.marstech.mtlinkspray.service.ShortenerService.getShortenedLink;
import static fr.marstech.mtlinkspray.utils.NetworkUtils.isValidUrl;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class ShortenerServiceImpl implements ShortenerService {

    final RandomIdGeneratorService randomIdGeneratorService;

    final LinkItemRepository linkItemRepository;

    public ShortenerServiceImpl(LinkItemRepository linkItemRepository, RandomIdGeneratorService randomIdGeneratorService) {
        this.linkItemRepository = linkItemRepository;
        this.randomIdGeneratorService = randomIdGeneratorService;
    }

    @Override
    public String shorten(String url, HttpServletRequest httpServletRequest) {
        if (isBlank(url)) {
            throw new IllegalArgumentException("URL must not be null or empty");
        }
        if (!isValidUrl(url)) {
            throw new IllegalArgumentException("Invalid URL: %s".formatted(url));
        }
        try {
            LinkItemTarget target = new LinkItemTarget(url);
            LinkItem linkItem = new LinkItem(getFreeUniqueId(), LocalDateTime.now(), null, true, null, Map.of(), new HistoryItem(), List.of(), target);
            LinkItem savedLinkItem = linkItemRepository.save(linkItem);
            return getShortenedLink(httpServletRequest, savedLinkItem.getId());
        } catch (Exception e) {
            throw new UrlShorteningException("Failed to shorten URL: %s".formatted(url), e);
        }
    }

    @Override
    public String getTarget(String uid, HttpServletRequest httpServletRequest) throws ChangeSetPersister.NotFoundException {
        return linkItemRepository.findById(uid).orElseThrow(ChangeSetPersister.NotFoundException::new).getTarget().getTargetUrl();
    }

    private String getFreeUniqueId() {
        return randomIdGeneratorService.getGeneratedFreeId();
    }
}
