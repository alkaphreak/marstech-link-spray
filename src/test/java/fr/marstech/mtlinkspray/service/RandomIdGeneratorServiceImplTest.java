package fr.marstech.mtlinkspray.service;

import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.stream.IntStream;

import static fr.marstech.mtlinkspray.config.TestConfig.MONGO_DB_DOCKER_IMAGE_NAME;
import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@Log
@Testcontainers
@SpringBootTest
class RandomIdGeneratorServiceImplTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse(MONGO_DB_DOCKER_IMAGE_NAME)).withReuse(true);

    @Autowired
    private RandomIdGeneratorServiceImpl randomIdGeneratorService;

    @Autowired
    private Environment environment;

    RandomIdGeneratorServiceImplTest() {
    }

    @Test
    void generateRandomId() {
        String randomId = randomIdGeneratorService.generateRandomId();
        assertNotNull(randomId);
        assertEquals(randomIdGeneratorService.length, randomId.length());
    }

    @Test
    void benchmarkWithAndWithoutCache() {
        int testRange = 1;
        long start;
        long end;
        long withCache;
        long withoutCache;

        // without cache
        start = System.currentTimeMillis();
        IntStream.range(0, testRange).forEach(i -> randomIdGeneratorService.getGeneratedFreeIdwithoutCache());
        end = System.currentTimeMillis();
        withoutCache = end - start;

        // with cache
        start = System.currentTimeMillis();
        IntStream.range(0, testRange).forEach(i -> randomIdGeneratorService.getGeneratedFreeIdWithCache());
        end = System.currentTimeMillis();
        withCache = end - start;

        log.info(format("With cache: {0}ms, Without cache: {1}ms, Difference: {2}ms, Test range: {3}, Cache depth: {4}, Cache treshold: {5}", withCache, withoutCache, withoutCache - withCache, testRange, randomIdGeneratorService.cacheDepth, randomIdGeneratorService.cacheTreshold));
    }
}