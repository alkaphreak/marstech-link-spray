package fr.marstech.mtlinkspray.service;

import fr.marstech.mtlinkspray.objects.RandomIdGeneratorObject;
import fr.marstech.mtlinkspray.repository.LinkItemRepository;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Boolean.TRUE;

@Log
@Service
public class RandomIdGeneratorServiceImpl implements RandomIdGeneratorService {

    final LinkItemRepository linkItemRepository;

    @Value("${mt.link-spray.random-id.length}")
    int length;

    @Value("${mt.link-spray.random-id.cache.enabled}")
    Boolean isCacheEnabled;

    @Value("${mt.link-spray.random-id.cache.depth}")
    Integer cacheDepth;

    @Value("${mt.link-spray.random-id.cache.treshold}")
    Integer cacheTreshold;

    final Deque<String> cacheIds = new ArrayDeque<>();

    @Value("${mt.link-spray.random-id.charset}")
    private String charset;

    @Value("${mt.link-spray.random-id.prefix}")
    private String prefix;

    public RandomIdGeneratorServiceImpl(LinkItemRepository linkItemRepository) {
        this.linkItemRepository = linkItemRepository;
    }

    @Override
    public String getGeneratedFreeId() {
        return TRUE.equals(isCacheEnabled) ? getGeneratedFreeIdWithCache() : getGeneratedFreeIdWithoutCache();
    }

    @Override
    public @NotNull String getGeneratedFreeIdWithoutCache() {
        String id;
        do {
            id = generate();
        } while (linkItemRepository.findById(id).isPresent());
        return id;
    }

    @Override
    public @NotNull String getGeneratedFreeIdWithCache() {
        if (cacheIds.size() < cacheTreshold) {
            Set<String> linkItemIds = linkItemRepository.findAllIds();
            while (cacheIds.size() < cacheTreshold) {
                cacheIds.addAll(generateRandomIds(cacheDepth).stream().filter(s -> !linkItemIds.contains(s)).collect(Collectors.toSet()));
            }
        }
        return cacheIds.pop();
    }

    public Set<String> generateRandomIds(int count) {
        return IntStream.range(0, count).mapToObj(i -> generate()).collect(Collectors.toSet());
    }

    @Override
    public String generateRandomId() {
        return generate();
    }

    private @NotNull String generate() {
        return RandomIdGeneratorObject.INSTANCE.generate(prefix, length, charset);
    }
}
