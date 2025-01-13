package fr.marstech.mtlinkspray.service;

import fr.marstech.mtlinkspray.config.AbstractBaseIntegrationTest;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.stream.IntStream;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

@Log
@Testcontainers
@SpringBootTest
class RandomIdGeneratorServiceImplTest extends AbstractBaseIntegrationTest {

    @Autowired
    private RandomIdGeneratorServiceImpl randomIdGeneratorService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void generateRandomId() {
        String randomId = randomIdGeneratorService.generateRandomId();
        assertNotNull(randomId);
        assertEquals(randomIdGeneratorService.length, randomId.length());
    }

    @Test
    void getGeneratedFreeIdWithCache() {
        String freeId = randomIdGeneratorService.getGeneratedFreeIdWithCache();
        assertEquals(randomIdGeneratorService.length, freeId.length());
    }

    @Test
    void getGeneratedFreeIdWithoutCache() {
        String freeId = randomIdGeneratorService.getGeneratedFreeIdwithoutCache();
        assertEquals(randomIdGeneratorService.length, freeId.length());
    }

    @Disabled
    @Test
    void benchmarkWithAndWithoutCache() {
        int testRange = 10000;

        long start = System.currentTimeMillis();
        randomIdGeneratorService.isCacheEnabled = true;
        IntStream.range(0, testRange).forEach(i -> randomIdGeneratorService.getGeneratedFreeId());
        long end = System.currentTimeMillis();
        long withCache = end - start;

        start = System.currentTimeMillis();
        randomIdGeneratorService.isCacheEnabled = false;
        IntStream.range(0, testRange).forEach(i -> randomIdGeneratorService.getGeneratedFreeId());
        end = System.currentTimeMillis();
        long withoutCache = end - start;

        log.info(format("With cache: {0}ms, Without cache: {1}ms, Difference: {2}ms, Test range: {3}, Cache depth: {4}, Cache treshold: {5}", withCache, withoutCache, withoutCache - withCache, testRange, randomIdGeneratorService.cacheDepth, randomIdGeneratorService.cacheTreshold));

        assertTrue(withCache < withoutCache);
    }
}