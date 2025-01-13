package fr.marstech.mtlinkspray.config;

import fr.marstech.mtlinkspray.repository.LinkItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public class AbstractBaseIntegrationTest {

    @Autowired
    private LinkItemRepository linkItemRepository;

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0.4").withExposedPorts(27017).withReuse(true);
}